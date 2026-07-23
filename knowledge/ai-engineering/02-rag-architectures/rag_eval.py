"""A retrieval eval harness that runs directly against AdvancedRAGPipeline from
advanced_rag.py — turning the "build a golden set, measure hit rate before answer
quality" advice in rag-concepts.md and 05-evaluation-observability/evals-and-testing.md
into something you actually run instead of just read about.

Retrieval hit rate is measured first and separately from answer faithfulness on
purpose: if the right chunk never reaches the LLM, no amount of prompt tuning on the
generation side can fix the answer. Debug retrieval before generation, always.

Install: pip install -r ../requirements.txt
Run:     python rag_eval.py
"""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger, timed  # noqa: E402
from advanced_rag import AdvancedRAGPipeline, RAGAnswer  # noqa: E402

logger = get_logger(__name__)


@dataclass
class GoldenCase:
    """One row of the eval set: a question plus what a correct retrieval/answer
    looks like. Keep this format close to a JSONL file in real usage — see
    rag-concepts.md's "Build a golden set" section for the on-disk shape this mirrors.
    """
    question: str
    expected_source: str  # which document should show up in the retrieved chunks
    expected_answer_contains: list[str] = field(default_factory=list)  # keywords the answer should mention


@dataclass
class CaseResult:
    case: GoldenCase
    retrieval_hit: bool
    answer_contains_expected: bool
    answer: RAGAnswer


@dataclass
class EvalReport:
    results: list[CaseResult]

    @property
    def retrieval_hit_rate(self) -> float:
        if not self.results:
            return 0.0
        return sum(r.retrieval_hit for r in self.results) / len(self.results)

    @property
    def answer_quality_rate(self) -> float:
        if not self.results:
            return 0.0
        return sum(r.answer_contains_expected for r in self.results) / len(self.results)

    def print_summary(self) -> None:
        print(f"\n{'Question':<55} {'Retrieval':<10} {'Answer':<10}")
        print("-" * 77)
        for r in self.results:
            retrieval = "HIT" if r.retrieval_hit else "MISS"
            answer = "OK" if r.answer_contains_expected else "WEAK"
            print(f"{r.case.question[:53]:<55} {retrieval:<10} {answer:<10}")

        print("-" * 77)
        print(f"Retrieval hit rate: {self.retrieval_hit_rate:.0%}  "
              f"(fix this first — generation can't save a wrong retrieval)")
        print(f"Answer quality rate: {self.answer_quality_rate:.0%}  "
              f"(keyword-based, use LLM-as-judge for nuanced grading)")


class RetrievalEvaluator:
    def __init__(self, pipeline: AdvancedRAGPipeline):
        self.pipeline = pipeline

    def evaluate(self, golden_set: list[GoldenCase], *, top_k: int = 5) -> EvalReport:
        results = []
        with timed(f"evaluating {len(golden_set)} case(s)", logger):
            for case in golden_set:
                answer = self.pipeline.query(case.question, top_k=top_k)
                results.append(self._score_case(case, answer))
        return EvalReport(results=results)

    @staticmethod
    def _score_case(case: GoldenCase, answer: RAGAnswer) -> CaseResult:
        retrieved_sources = {chunk.source for chunk in answer.sources}
        retrieval_hit = case.expected_source in retrieved_sources

        answer_lower = answer.answer.lower()
        answer_contains_expected = (
            all(kw.lower() in answer_lower for kw in case.expected_answer_contains)
            if case.expected_answer_contains
            else True  # no keyword expectation set — don't penalize what wasn't specified
        )

        if not retrieval_hit:
            logger.warning(
                "retrieval MISS for %r — expected source %r, got %r",
                case.question, case.expected_source, retrieved_sources,
            )

        return CaseResult(
            case=case,
            retrieval_hit=retrieval_hit,
            answer_contains_expected=answer_contains_expected,
            answer=answer,
        )


DEMO_GOLDEN_SET = [
    GoldenCase(
        question="How do I roll back a bad deployment?",
        expected_source="deploy-runbook.md",
        expected_answer_contains=["rollout undo"],
    ),
    GoldenCase(
        question="How does a new engineer get access to the staging cluster?",
        expected_source="onboarding.md",
        expected_answer_contains=["staging"],
    ),
    GoldenCase(
        question="What's the company's stance on remote work?",  # deliberately unanswerable
        expected_source="__no_source_should_exist__",
        expected_answer_contains=[],
    ),
]


def _demo() -> None:
    """Mirrors the toy corpus in advanced_rag.py's _demo() so this runs standalone.
    In real usage, evaluate() runs against a pipeline already indexed with your own
    corpus and a golden set with cases pulled from real production questions/failures.
    """
    sample_docs = {
        "deploy-runbook.md": (
            "To roll back a bad deploy, run `kubectl rollout undo deployment/api`. "
            "This reverts to the previous ReplicaSet within roughly 30 seconds."
        ),
        "onboarding.md": (
            "New engineers get repo access via the platform team's Slack channel. "
            "Request access to the monorepo and the staging Kubernetes cluster on day one."
        ),
    }

    pipeline = AdvancedRAGPipeline()
    pipeline.index_documents(sample_docs)

    evaluator = RetrievalEvaluator(pipeline)
    report = evaluator.evaluate(DEMO_GOLDEN_SET)
    report.print_summary()

    # The unanswerable case is the interesting one: a correct system says "I don't
    # know" rather than hallucinating a policy that was never in the indexed corpus.
    # A retrieval MISS on that specific case is actually the desired outcome.


if __name__ == "__main__":
    _demo()
