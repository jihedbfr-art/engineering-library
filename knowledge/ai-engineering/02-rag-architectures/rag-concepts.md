# RAG — Retrieval-Augmented Generation

## The idea

LLMs don't know your data. RAG fetches the relevant pieces at question-time and hands them to the model:

```
                     ┌────────────── INDEXING (offline) ──────────────┐
docs → chunk → embed → store in vector DB                             │
                     └───────────────────────────────────────────────┘
                     ┌────────────── QUERY (online) ──────────────────┐
question → embed → search top-k chunks → build prompt with chunks     │
        → LLM answers USING the chunks → answer + citations           │
                     └───────────────────────────────────────────────┘
```

## Minimal implementation sketch (Python)

```python
# Indexing
chunks = split(documents, size=800, overlap=150)      # by tokens, respect headings
vectors = embed(chunks)                                # embedding model
db.upsert(zip(ids, vectors, chunks))                   # pgvector / Qdrant / Chroma

# Query
q_vec = embed(question)
hits = db.search(q_vec, top_k=5)
prompt = f"""Answer using ONLY the sources below. Cite [n]. If the sources
don't contain the answer, say so.

Sources:
{format_numbered(hits)}

Question: {question}"""
answer = llm(prompt)
```

## The decisions that make or break quality

| Decision | Guidance |
|---|---|
| **Chunk size** | 500–1000 tokens, 10–20% overlap; split on structure (headings, paragraphs), never mid-sentence |
| **top_k** | 3–8. More = noise + cost; if you need 20 chunks, retrieval is failing |
| **Hybrid search** | Vector + keyword (BM25) beats either alone — exact terms (error codes, names) need keywords |
| **Reranking** | A cross-encoder reranker on top-30 → top-5 is the cheapest big quality win |
| **Metadata filters** | Filter by source/date/team *before* semantic search when you can |

## Evaluate or you're flying blind

Build a test set of (question → expected source, expected answer) pairs. Track:
- **Retrieval hit rate** — did the right chunk make top-k? (if not, generation can't save you)
- **Faithfulness** — is the answer supported by the retrieved text?
- **Answer quality** — human or LLM-as-judge scoring

Most "the AI answers wrong" bugs are retrieval bugs. Debug retrieval first.

## Classic failure modes

1. **Garbage chunks** — PDFs parsed into soup. Fix parsing before anything else.
2. **Stale index** — docs changed, index didn't. Automate re-indexing.
3. **Question/chunk vocabulary gap** — users say "crash", docs say "unhandled exception". Hybrid search + query rewriting help.
4. **Context stuffing** — dumping 40 chunks "to be safe" degrades answers and costs 10×.
