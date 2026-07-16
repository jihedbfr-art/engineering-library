# AI Engineer Roadmap

Building applications *with* AI (LLM engineering) — the fastest-growing dev role.

```
Stage 1           Stage 2            Stage 3           Stage 4             Stage 5
FOUNDATIONS   →   API MASTERY    →   RAG & DATA    →   AGENTS & TOOLS  →   PRODUCTION
python, APIs,     prompting,         embeddings,       tool use, agent     evals, cost,
how LLMs work     structured out,    vector DBs,       loops, MCP,         security, obs,
                  streaming          chunking, evals   orchestration       fine-tuning
```

## Stage 1 — Foundations

- Python solid (async, typing, packaging)
- [llm-fundamentals](../../ai/llm/llm-fundamentals.md): tokens, context, temperature, embeddings
- ✅ *You can*: explain to a colleague why the model "hallucinates" and what a context window is.

## Stage 2 — API mastery

- [Prompt patterns](../../ai/prompt-engineering/patterns.md): role/task/constraints/format, few-shot, delimiting
- Structured output (JSON schema), streaming, error handling & retries
- Version prompts + build a 20-case eval set
- ✅ *You can*: a prompt change is measured against the eval set before shipping, like any code change.

## Stage 3 — RAG & data

- [rag.md](../../ai/llm/rag.md): chunking, hybrid search, reranking, metadata filters
- One vector store hands-on (pgvector is a great default)
- Retrieval evals: hit rate before answer quality
- ✅ *You can*: build a Q&A over your own docs that cites sources and says "I don't know".

## Stage 4 — Agents & tools

- [building-agents](../../ai/agents/building-agents.md): tool schemas, the loop, bounding cost/steps
- MCP (Model Context Protocol) for standardized tool servers
- Human gates on irreversible actions; sandboxed execution
- ✅ *You can*: an agent that researches + writes a report using 3 tools, under a cost cap.

## Stage 5 — Production

- **Evals as CI** — no eval, no deploy
- **Security**: prompt injection defenses, OWASP LLM Top 10, least-privilege tools
- **Cost & latency**: caching, model routing (small model for easy tasks), batching
- **Observability**: log every call (redacted), track tokens/cost/quality drift
- Fine-tuning only when prompting + RAG demonstrably plateau
- ✅ *You can*: show a dashboard of quality, cost and latency for your AI feature — and explain a regression.

## The mindset shift

Traditional code is deterministic; LLM apps are probabilistic. Your job becomes:
1. Constrain the space of outputs (prompts, schemas, retrieval)
2. Measure relentlessly (evals over vibes)
3. Design for graceful failure (fallbacks, human gates)

The engineers who win are the ones who treat AI outputs like untrusted user input: validate, bound, monitor.
