# Why the vector store needs a seam, not just a client import

[`02-rag-architectures/advanced_rag.py`](../02-rag-architectures/advanced_rag.py) uses Chroma
directly, and says so in its own docstring: swapping to pgvector or Qdrant later should only
touch `VectorStore.__init__/upsert/search`. This module is that promise made real — a small
interface those three methods actually implement against, instead of Chroma calls scattered
through a pipeline that has to be rewritten every time the backend changes.

I didn't build this into the RAG script itself on day one, and I don't think that was wrong —
you don't know what the seam needs to look like until you've written the concrete version once
and felt where the vendor-specific bits actually live. Abstracting before that point tends to
produce an interface shaped like nothing in particular. This is the version that came after.

## What actually varies between backends

Not as much as vendor docs make it seem, and not as little as "just swap the client" implies.

- **Chroma / Qdrant / Weaviate / Pinecone** — all managed-ish, all speak roughly "upsert vectors
  with metadata, search by vector with a filter." The differences are in filter syntax,
  namespace/collection semantics, and how each one wants metadata typed.
- **pgvector** — a Postgres extension, not a separate service. If the rest of the app's data is
  already in Postgres, this removes an entire service from the deployment, at the cost of index
  types that aren't as fast at very large scale as the vector-native options.
- **A plain in-memory list** — genuinely fine for tests, demos, and anything under a few thousand
  vectors. Don't reach for a managed vector DB before you've actually outgrown this; it's a real
  option, not just a stub.

## The interface this module settles on

Three methods, deliberately not more: `upsert(chunks)`, `search(query_vector, top_k, filters)`,
`delete(ids)`. Everything else — connection setup, auth, retries — lives inside a specific
backend's `__init__`, not in the interface, because that's exactly the part that's supposed to
differ and shouldn't leak into code that calls `search()`.

[`vector_store.py`](vector_store.py) implements this as a `Protocol` (structural typing — a class
doesn't need to inherit from anything to satisfy it, it just needs the right methods), with two
concrete backends: `InMemoryVectorStore` (numpy cosine similarity, zero external dependencies,
what the demo actually runs) and `ChromaVectorStore` (a thin adapter over the client already used
in `02-rag-architectures`). `VectorStore.create(backend="auto")` picks Chroma if it's importable,
falls back to in-memory otherwise — so the demo runs on a machine with no `chromadb` installed at
all, and upgrades itself automatically on one that has it.

## What this doesn't solve

Filter syntax is not actually unified here — `filters` is passed through as a plain dict and each
backend interprets it its own way, because building a real cross-vendor filter DSL is a much
bigger project than this file, and a fake unification that silently drops filters a backend
doesn't understand would be worse than an honest "this part still varies." If you're building
this for real, that's the next seam to design, not this one.
