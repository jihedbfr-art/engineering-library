# 🤖 AI Engineering

Production-grade RAG, agents, and local inference — not another notebook of demos.

![Python](https://img.shields.io/badge/python-3.11%2B-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Status](https://img.shields.io/badge/status-active-brightgreen)
![RAG](https://img.shields.io/badge/retrieval-Chroma%20%2B%20BM25-purple)
![Agents](https://img.shields.io/badge/agents-CrewAI-orange)
![Local Inference](https://img.shields.io/badge/local%20inference-Ollama-black)
![Identity](https://img.shields.io/badge/agent%20identity-Keycloak%20%2F%20OIDC-1a1a2e)

## What this is

A working, opinionated slice of applied AI engineering: hybrid **RAG** (retrieval-augmented
generation), bounded **multi-agent systems**, and **local LLM** inference you can run without
an API key. Every module pairs a concept doc with real, runnable Python — not a snippet that
only makes sense in a slide.

I built this the way I'd want to hand it to a new engineer joining an AI feature team: enough
theory to know *why*, enough working code to know *how*, and honest notes on where each
approach breaks down. If you're evaluating whether I can build this stuff in production, this
is closer to the truth than a portfolio of toy chatbots.

> A terminal recording and an architecture diagram belong here — see [`assets/`](assets/).
> Not adding a placeholder GIF that doesn't exist yet; it'll land when there's a real one.

## Quick start

```bash
git clone https://github.com/jihedbfr-art/dev-library.git
cd dev-library/ai
pip install -r requirements.txt

# pick one:
python 02-rag-architectures/advanced_rag.py          # hybrid RAG demo, needs OPENAI_API_KEY + ANTHROPIC_API_KEY
python 03-agentic-workflows/research_agent_crew.py "your topic here"   # needs OPENAI_API_KEY
python 04-local-inference/local_ollama_chat.py "your question here"    # needs a running `ollama serve`, no API key
```

Two minutes to a working RAG query, agent crew, or local model chat — pick whichever matches
what you're evaluating.

## Why this over a tutorial repo

- **Hybrid retrieval, not vector-search-and-hope.** Dense (Chroma) and sparse (BM25) search
  fused with reciprocal rank fusion, then optionally reranked with a cross-encoder — because
  pure semantic search quietly loses exact-term queries (error codes, SKUs, names) that
  keyword search catches for free.
- **Multi-agent systems with real cost bounds**, not an unbounded loop that racks up a bill —
  `max_iter` and `max_rpm` are set deliberately, and the tool boundary between the Researcher
  and Writer agent is designed on purpose, not an accident of the framework's defaults.
- **A local-inference path that costs nothing per token.** The Ollama client isn't a toy —
  it implements self-consistency sampling and iterative self-refinement, two real test-time
  scaling techniques, so a small local model can trade compute time for answer quality instead
  of needing a bigger hosted model.
- **Hooks and skills, not just tools.** A `HookRegistry` that can veto or rewrite a tool call
  mid-loop (fails closed on error, not open), and a two-phase skill loader that keeps context
  cost proportional to what's actually used — a framework-agnostic take on a pattern that's
  spreading fast across agent tooling right now.
- **Evaluation is a first-class module, not a footnote.** `05-evaluation-observability` exists
  because every technique above produces probabilistic output, and "it looked right" is not a
  metric — this module wasn't in the original plan I started from, and I added it because
  skipping it would make everything else in here unverifiable.
- **One shared foundation, not three copy-pasted `try/except` blocks.** Logging, retry-with-backoff,
  and typed errors live in `shared/` and get imported by every script — the same discipline
  you'd expect in a real service codebase, applied to AI engineering code.

## Architecture

| Module | Purpose |
|---|---|
| [`01-foundations/`](01-foundations/) | Tokens, embeddings, prompt patterns — the primitives every other module assumes |
| [`02-rag-architectures/`](02-rag-architectures/) | Hybrid retrieval pipeline: Chroma + BM25 + Reciprocal Rank Fusion + cross-encoder rerank |
| [`03-agentic-workflows/`](03-agentic-workflows/) | Bounded multi-agent orchestration with CrewAI (Researcher + Writer) |
| [`04-local-inference/`](04-local-inference/) | Ollama-based local chat with self-consistency and iterative-refinement test-time scaling |
| [`05-evaluation-observability/`](05-evaluation-observability/) | Golden eval sets, scoring methods, CI gating — how you know any of the above actually works |
| [`06-agent-hooks-and-skills/`](06-agent-hooks-and-skills/) | Hooks for controlling an agent loop (veto/rewrite tool calls), 11 real skill packages, 4 composable hook recipes |
| [`07-extensibility/`](07-extensibility/) | Plugin/connector pattern: discoverable external tools with an enforced, least-privilege permission boundary |
| [`08-guards-safety/`](08-guards-safety/) | Input/output guard chain — PII and injection checks going in, leak checks coming out, fail-closed by default |
| [`09-vector-layer/`](09-vector-layer/) | Backend-agnostic `VectorStore` interface — in-memory and Chroma today, pgvector/Qdrant are a new adapter, not a rewrite |
| [`10-model-routing-and-cost/`](10-model-routing-and-cost/) | Rule-based model tier routing, a semantic cache built on the vector layer, and per-call cost tracking |
| [`11-agent-identity-and-access/`](11-agent-identity-and-access/) | Per-agent OAuth2/OIDC clients via Keycloak, scoped and short-lived tokens, fail-closed scope enforcement |
| [`shared/`](shared/) | Logging, retry/backoff, and error types reused across every script |

## Where to start

New to this space: read `01-foundations` in order, then `02-rag-architectures` — RAG is the
highest-leverage, lowest-risk place to start building. Already building agents or running
local models: jump straight to `03` or `04`, the docs don't assume you've read the others.

---

Crafted with passion — By JiHéD
