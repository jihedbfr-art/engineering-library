# Evaluating LLM Applications

The single skill that separates a demo from a product. If you can't measure it, you can't improve it — and "it felt good" is not a metric.

## Why evals, not vibes

LLM output is probabilistic. A prompt change that fixes one case can silently break five others. Without an eval set, every "improvement" is a gamble you can't see the result of.

## Build a golden set (start with 20–50 cases)

```jsonc
// evals/notes-qa.jsonl — one case per line
{"input": "How do I restore a deleted note?",
 "expected_source": "trash.md",
 "expected_contains": ["restore", "trash"]}
{"input": "What port is Keycloak on?",
 "expected_contains": ["8090"]}
```
Cover: common cases, edge cases, adversarial inputs, and known past failures (every bug becomes a permanent test).

## Scoring methods (cheapest → most robust)

| Method | Good for | Cost |
|---|---|---|
| **Exact / regex match** | structured output, specific facts | free, fast |
| **Contains / keyword** | must-mention checks | free |
| **Embedding similarity** | "is the answer roughly right" | cheap |
| **LLM-as-judge** | nuanced quality, faithfulness | model call per case |
| **Human review** | final gate, judge calibration | expensive |

## LLM-as-judge (use carefully)

```
Rate the ANSWER against the QUESTION and SOURCES on a 1–5 scale for:
- faithfulness (only claims supported by sources)
- relevance (answers what was asked)
Reply JSON: {"faithfulness": n, "relevance": n, "reason": "..."}
```
Cautions: judges are biased (toward longer answers, toward their own style). Calibrate against ~20 human-scored cases; use a strong model as judge; make the rubric concrete.

## Metrics for RAG specifically

1. **Retrieval hit rate** — did the right chunk reach top-k? *(fix this first; generation can't fix bad retrieval)*
2. **Faithfulness** — answer grounded in retrieved text?
3. **Answer relevance** — actually addresses the question?
4. **Context precision** — how much retrieved text was noise?

## Wire it into CI

```yaml
  eval:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: python evals/run.py --set evals/notes-qa.jsonl --threshold 0.85
        # exit non-zero if pass rate < threshold → blocks the merge
```
No eval green, no deploy — same discipline as unit tests.

## What to track over time

- Pass rate per eval set (watch for regressions)
- Cost per request and p95 latency (quality that triples cost isn't free)
- Drift: re-run evals when you change model, prompt, or data

## Common mistakes

- ❌ Testing on the examples you used to write the prompt (overfitting) → keep a held-out set.
- ❌ One giant score → break it down (faithfulness vs relevance fail for different reasons).
- ❌ Never updating the set → add every production failure as a new case.
