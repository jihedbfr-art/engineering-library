# 02 — RAG Architectures

RAG is the answer to "the model doesn't know my data" — and the module most projects get
wrong first, usually not because of the LLM call but because of what happens before it.

[rag-concepts.md](rag-concepts.md) covers the theory: chunking, hybrid search, reranking,
metadata filtering, and the eval loop that tells you which of those actually needs fixing.

[advanced_rag.py](advanced_rag.py) is a working pipeline, not a slide: paragraph-aware
chunking, Chroma for dense vector search, BM25 for sparse keyword search, Reciprocal Rank
Fusion to combine them, and an optional cross-encoder rerank pass before anything reaches the
LLM. Swap points are marked in the module docstring if you want Supabase/pgvector instead of
Chroma, or a different embedding/completion provider.

If retrieval quality is bad, fix retrieval before touching the prompt — that's not a slogan,
it's the order of operations that actually saves time.
[rag_eval.py](rag_eval.py) makes that concrete: a golden-set harness that runs against
`AdvancedRAGPipeline` directly, scoring retrieval hit rate separately from answer quality — a
retrieval miss is a different bug than a bad generation, and conflating the two into one pass/fail
number is how RAG debugging goes in circles. See
[05-evaluation-observability](../05-evaluation-observability/) for the scoring methods this
harness is a concrete instance of.
