"""Chat client for locally-hosted models via Ollama (Llama 3, DeepSeek, Mistral, etc.),
with two test-time-scaling strategies layered on top of a plain chat call: self-consistency
sampling and iterative self-refinement. Neither requires a model natively trained for
extended reasoning (like DeepSeek-R1) — they trade extra local inference time, which is
free once you own the hardware, for answer quality. That tradeoff is the whole point of
running models locally instead of paying per-token for a hosted frontier model on every
call.

Requires a running Ollama daemon: https://ollama.com — `ollama pull llama3` (or your
model of choice) before running this script.

Install: pip install -r ../requirements.txt
Run:     python local_ollama_chat.py "Why does connection pooling reduce database latency?"
"""

from __future__ import annotations

import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import LLMCallError, get_logger, retry_with_backoff, timed  # noqa: E402

logger = get_logger(__name__)

DEFAULT_MODEL = "llama3"
DEFAULT_HOST = "http://localhost:11434"


def _client():
    try:
        import ollama
    except ImportError as exc:
        raise LLMCallError("ollama package not installed. Run: pip install ollama") from exc
    return ollama


@retry_with_backoff(max_attempts=3, base_delay_seconds=2.0, retryable_exceptions=(Exception,))
def _chat(messages: list[dict], model: str, temperature: float) -> str:
    ollama = _client()
    try:
        response = ollama.chat(
            model=model,
            messages=messages,
            options={"temperature": temperature},
        )
    except Exception as exc:  # noqa: BLE001 - narrowed to LLMCallError for callers
        raise LLMCallError(
            f"Ollama call failed ({exc}). Is the daemon running? Try: ollama serve"
        ) from exc
    return response["message"]["content"]


def simple_chat(prompt: str, *, model: str = DEFAULT_MODEL, system: str | None = None) -> str:
    """One-shot chat call — the baseline every other function here builds on."""
    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": prompt})

    with timed(f"chat ({model})", logger):
        return _chat(messages, model=model, temperature=0.7)


def self_consistency_answer(
    prompt: str, *, model: str = DEFAULT_MODEL, samples: int = 5, temperature: float = 0.8
) -> tuple[str, list[str]]:
    """Sample the model N times at nonzero temperature and return the most common
    answer among the samples, alongside all of them for inspection. This is the
    "self-consistency" test-time scaling technique: for tasks with a discrete-ish
    correct answer (a calculation, a classification, a yes/no with reasoning), the
    modal answer across samples is more reliable than any single greedy decode,
    because errors in individual reasoning chains tend not to agree with each other.

    Not free: `samples` full generations instead of one. Reserve it for calls where
    correctness matters more than latency.
    """
    logger.info("sampling %d candidates for self-consistency check", samples)
    candidates = []
    for i in range(samples):
        answer = simple_chat(
            f"{prompt}\n\nThink step by step, then give your final answer on the last line "
            f"prefixed with 'FINAL:'.",
            model=model,
        )
        candidates.append(answer)
        logger.debug("sample %d/%d done", i + 1, samples)

    finals = [_extract_final(c) for c in candidates]
    most_common, count = Counter(finals).most_common(1)[0]
    logger.info("consensus answer appeared in %d/%d samples", count, samples)
    return most_common, candidates


def _extract_final(text: str) -> str:
    for line in reversed(text.strip().splitlines()):
        if line.strip().upper().startswith("FINAL:"):
            return line.split(":", 1)[1].strip()
    return text.strip().splitlines()[-1] if text.strip() else ""


def iterative_refinement(
    prompt: str, *, model: str = DEFAULT_MODEL, max_rounds: int = 3
) -> str:
    """Draft, critique, revise — bounded by max_rounds. This is the cheap local
    substitute for a model with native extended-thinking tokens: instead of one
    hidden long reasoning trace, you get an explicit, inspectable loop of visible
    drafts and critiques, which is arguably better for debugging even when a
    reasoning-native model is available.

    Stops early if the critique step reports no more issues, so simple prompts
    don't pay for rounds they don't need.
    """
    draft = simple_chat(prompt, model=model)
    logger.info("initial draft generated (%d chars)", len(draft))

    for round_num in range(1, max_rounds + 1):
        critique_prompt = (
            f"Original question: {prompt}\n\nDraft answer:\n{draft}\n\n"
            "Critique this draft: list concrete errors, gaps, or unclear points. "
            "If it's already correct and complete, reply with exactly 'NO ISSUES'."
        )
        critique = simple_chat(critique_prompt, model=model, temperature=0.3)

        if critique.strip().upper().startswith("NO ISSUES"):
            logger.info("converged after %d refinement round(s)", round_num - 1)
            break

        revise_prompt = (
            f"Original question: {prompt}\n\nDraft answer:\n{draft}\n\n"
            f"Critique:\n{critique}\n\nWrite an improved answer that addresses the critique."
        )
        draft = simple_chat(revise_prompt, model=model, temperature=0.5)
        logger.info("refinement round %d/%d complete", round_num, max_rounds)

    return draft


def main() -> None:
    prompt = " ".join(sys.argv[1:]) or "What's the tradeoff between eager and lazy loading in an ORM?"
    model = DEFAULT_MODEL

    print(f"\n--- simple_chat ({model}) ---")
    try:
        print(simple_chat(prompt, model=model))
    except LLMCallError as exc:
        logger.error(str(exc))
        sys.exit(1)

    print(f"\n--- iterative_refinement ({model}, max 3 rounds) ---")
    print(iterative_refinement(prompt, model=model, max_rounds=3))


if __name__ == "__main__":
    main()
