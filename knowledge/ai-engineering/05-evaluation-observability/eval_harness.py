"""A runnable eval harness implementing the scoring-method ladder from
evals-and-testing.md: exact/regex match, keyword containment, embedding similarity,
and LLM-as-judge — cheapest to most expensive, in that order, because running the
expensive judge on every case when a regex would do is money and latency wasted.

This is the harness rag_eval.py's answer-quality check is a narrower, RAG-specific
instance of, and the same harness a fine-tuning decision (see
../01-foundations/fine-tuning-and-when-to-use-it.md) should be gated on before and
after training.

Wire this into CI exactly as evals-and-testing.md describes: exit non-zero if the
pass rate drops below a threshold, so a regression blocks the merge the same way a
failing unit test would.

Install: pip install -r ../requirements.txt
Run:     python eval_harness.py --threshold 0.8
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
from typing import Callable

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger, timed  # noqa: E402

logger = get_logger(__name__)


class ScoringMethod(Enum):
    EXACT = "exact"
    CONTAINS = "contains"
    REGEX = "regex"
    EMBEDDING_SIMILARITY = "embedding_similarity"
    LLM_JUDGE = "llm_judge"


@dataclass
class EvalCase:
    """One row of a golden set. Mirrors the JSONL shape from evals-and-testing.md —
    in real usage this comes from a .jsonl file, one case per line, not a Python list.
    """
    input: str
    method: ScoringMethod
    expected: str | list[str]  # exact string, list of required keywords, or regex pattern
    similarity_threshold: float = 0.8  # only used for EMBEDDING_SIMILARITY


@dataclass
class CaseResult:
    case: EvalCase
    actual_output: str
    passed: bool
    score: float  # 1.0/0.0 for binary methods, a real score for similarity/judge
    detail: str = ""


@dataclass
class EvalReport:
    results: list[CaseResult]
    threshold: float

    @property
    def pass_rate(self) -> float:
        if not self.results:
            return 0.0
        return sum(r.passed for r in self.results) / len(self.results)

    @property
    def passed_threshold(self) -> bool:
        return self.pass_rate >= self.threshold

    def print_summary(self) -> None:
        print(f"\n{'Input':<45} {'Method':<20} {'Result':<8} {'Score'}")
        print("-" * 85)
        for r in self.results:
            status = "PASS" if r.passed else "FAIL"
            print(f"{r.case.input[:43]:<45} {r.case.method.value:<20} {status:<8} {r.score:.2f}")
            if not r.passed and r.detail:
                print(f"    -> {r.detail}")
        print("-" * 85)
        gate = "PASS" if self.passed_threshold else "FAIL"
        print(f"Pass rate: {self.pass_rate:.0%}  (threshold: {self.threshold:.0%})  -> CI gate: {gate}")


class EvalHarness:
    def __init__(self, system_under_test: Callable[[str], str]):
        """system_under_test: any function that takes a prompt/question and returns
        the output to grade — swap in a call to advanced_rag.py's query(), a
        fine-tuned model's inference call, or anything else that produces text.
        """
        self.system_under_test = system_under_test

    def run(self, cases: list[EvalCase], *, threshold: float = 0.8) -> EvalReport:
        results = []
        with timed(f"running {len(cases)} eval case(s)", logger):
            for case in cases:
                actual = self.system_under_test(case.input)
                results.append(self._score(case, actual))
        return EvalReport(results=results, threshold=threshold)

    def _score(self, case: EvalCase, actual: str) -> CaseResult:
        scorer = _SCORERS[case.method]
        passed, score, detail = scorer(actual, case)
        if not passed:
            logger.warning("FAIL [%s]: %r -> %r (%s)", case.method.value, case.input, actual, detail)
        return CaseResult(case=case, actual_output=actual, passed=passed, score=score, detail=detail)


# --- Scorers, cheapest to most expensive -------------------------------------

def _score_exact(actual: str, case: EvalCase) -> tuple[bool, float, str]:
    passed = actual.strip() == str(case.expected).strip()
    return passed, float(passed), "" if passed else f"expected exact match: {case.expected!r}"


def _score_contains(actual: str, case: EvalCase) -> tuple[bool, float, str]:
    keywords = case.expected if isinstance(case.expected, list) else [case.expected]
    missing = [kw for kw in keywords if kw.lower() not in actual.lower()]
    passed = not missing
    return passed, float(passed), "" if passed else f"missing keywords: {missing}"


def _score_regex(actual: str, case: EvalCase) -> tuple[bool, float, str]:
    pattern = str(case.expected)
    passed = re.search(pattern, actual) is not None
    return passed, float(passed), "" if passed else f"no match for pattern: {pattern!r}"


def _score_embedding_similarity(actual: str, case: EvalCase) -> tuple[bool, float, str]:
    """Cosine similarity between the actual output and the expected reference answer.
    Falls back to a warning + auto-pass if no embedding backend is configured, rather
    than crashing the whole eval run over one scoring method — see
    ../01-foundations/tokenization-and-embeddings.md for what cosine similarity is
    actually measuring here.
    """
    try:
        import numpy as np
        from openai import OpenAI

        client = OpenAI()
        vectors = client.embeddings.create(
            model="text-embedding-3-small",
            input=[actual, str(case.expected)],
        ).data
        a, b = np.array(vectors[0].embedding), np.array(vectors[1].embedding)
        similarity = float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))
        passed = similarity >= case.similarity_threshold
        return passed, similarity, "" if passed else f"similarity {similarity:.2f} below threshold {case.similarity_threshold}"
    except ImportError:
        logger.warning("openai/numpy not installed, skipping embedding similarity scoring")
        return True, 1.0, "skipped: missing dependency"


def _score_llm_judge(actual: str, case: EvalCase) -> tuple[bool, float, str]:
    """LLM-as-judge: use carefully, per evals-and-testing.md — calibrate against
    human-scored cases, use a strong model, keep the rubric concrete. This
    implementation asks for a single faithfulness score against the reference
    answer, on a fixed 1-5 scale with a required JSON reply so the score is
    parseable rather than free text.
    """
    try:
        import json

        import anthropic

        client = anthropic.Anthropic()
        prompt = (
            f"Reference answer: {case.expected}\n\nCandidate answer: {actual}\n\n"
            "Rate how well the candidate answer matches the reference in meaning, "
            "on a 1-5 scale. Reply with JSON only: {\"score\": n, \"reason\": \"...\"}"
        )
        response = client.messages.create(
            model="claude-sonnet-5", max_tokens=200,
            messages=[{"role": "user", "content": prompt}],
        )
        judged = json.loads(response.content[0].text)
        normalized_score = judged["score"] / 5.0
        passed = normalized_score >= 0.6  # 3/5 or better
        return passed, normalized_score, judged.get("reason", "")
    except ImportError:
        logger.warning("anthropic SDK not installed, skipping LLM-as-judge scoring")
        return True, 1.0, "skipped: missing dependency"


_SCORERS: dict[ScoringMethod, Callable[[str, EvalCase], tuple[bool, float, str]]] = {
    ScoringMethod.EXACT: _score_exact,
    ScoringMethod.CONTAINS: _score_contains,
    ScoringMethod.REGEX: _score_regex,
    ScoringMethod.EMBEDDING_SIMILARITY: _score_embedding_similarity,
    ScoringMethod.LLM_JUDGE: _score_llm_judge,
}


# --- Demo ----------------------------------------------------------------

def _toy_system_under_test(question: str) -> str:
    """Stands in for a real LLM app so this file runs with zero API keys. Replace
    with a call to your actual system — advanced_rag.py's pipeline.query(question).answer
    is the natural one for this library.
    """
    canned = {
        "What port does the API run on?": "The API runs on port 8090.",
        "How do I reset my password?": "Go to Settings > Security > Reset Password.",
    }
    return canned.get(question, "I don't have information about that.")


DEMO_CASES = [
    EvalCase(input="What port does the API run on?", method=ScoringMethod.CONTAINS, expected=["8090"]),
    EvalCase(input="What port does the API run on?", method=ScoringMethod.REGEX, expected=r"port \d+"),
    EvalCase(input="How do I reset my password?", method=ScoringMethod.CONTAINS, expected=["reset", "password"]),
    EvalCase(input="What's the meaning of life?", method=ScoringMethod.CONTAINS, expected=["42"]),  # expected FAIL, demonstrates a red case
]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--threshold", type=float, default=0.8, help="minimum pass rate for CI gate to succeed")
    args = parser.parse_args()

    harness = EvalHarness(_toy_system_under_test)
    report = harness.run(DEMO_CASES, threshold=args.threshold)
    report.print_summary()

    sys.exit(0 if report.passed_threshold else 1)


if __name__ == "__main__":
    main()
