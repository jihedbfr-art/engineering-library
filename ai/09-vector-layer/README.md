# 09 — Vector Layer

The seam [`02-rag-architectures/advanced_rag.py`](../02-rag-architectures/advanced_rag.py) already
promises in its own docstring — "swapping Chroma for pgvector or Qdrant should only touch
`VectorStore.__init__/upsert/search`" — made real instead of implied.

[`vector-store-abstraction.md`](vector-store-abstraction.md) covers what actually varies between
vector DB backends versus what vendor docs make it look like varies, and why the interface here
stays to three methods on purpose.

[`vector_store.py`](vector_store.py) is the interface itself: a `VectorBackend` protocol, an
`InMemoryVectorStore` (numpy cosine similarity, no dependencies, what the demo runs by default),
a `ChromaVectorStore` adapter over the same client the RAG module uses, and a `VectorStore.create()`
factory that picks Chroma when it's installed and falls back to in-memory otherwise.

This slots directly under [`02-rag-architectures`](../02-rag-architectures/) — the retrieval
pipeline decides *what* to fetch and *how to rank it*; this module decides *where the vectors
actually live*.
