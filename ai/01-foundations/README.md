# 01 — Foundations

Everything downstream (RAG, agents, local inference) assumes you're comfortable with what's
in here. Skipping it is how people end up debugging a "broken" RAG pipeline that's actually
a tokenization or chunking problem three layers up.

- [llm-fundamentals.md](llm-fundamentals.md) — what an LLM does, the API mental model, when to
  reach for prompting vs. RAG vs. fine-tuning
- [ml-fundamentals.md](ml-fundamentals.md) — the classic-ML base underneath embeddings and evals,
  and where gradient-boosted trees still beat an LLM
- [tokenization-and-embeddings.md](tokenization-and-embeddings.md) — how text becomes tokens and
  vectors, and the failure modes that follow from getting either one wrong
- [prompt-engineering/](prompt-engineering/) — patterns that hold up in production, plus a
  copy-paste template library

Read in that order if you're new to this; jump straight to whichever file if you're not.
