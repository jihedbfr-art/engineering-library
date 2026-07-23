# Tokenization & Embeddings

The two primitives everything else in this library is built on. Skip this file and RAG, agents,
and fine-tuning all turn into cargo-culted API calls — you'll be able to make them work but not
tell why they broke.

## Tokenization — the model doesn't see words

A tokenizer splits text into subword units (BPE — byte-pair encoding, or one of its variants)
before anything reaches the model. Common English words are usually one token; rare words,
typos, code identifiers and non-English text get chopped into several.

```python
# Illustrative — real tokenizers differ by model/vendor, but the shape is universal
"connection"        -> ["connection"]                  # 1 token, common word
"unhandledException" -> ["unh", "andled", "Exception"]  # 3 tokens, camelCase splits badly
"Bonjour"            -> ["Bon", "jour"]                 # non-English often costs more tokens
```

Why this matters in practice, not just trivia:

- **Cost and context budget are counted in tokens, not characters.** A French or Arabic prompt
  can burn 1.5–2× the tokens of the English equivalent for the same meaning — I noticed this the
  hard way comparing bilingual prompts, and it changes the economics if you're building for a
  non-English-first market.
- **Numbers and code tokenize unevenly.** `"12345"` might be one token or five depending on the
  tokenizer's digit-grouping rules — this is part of why LLMs are shaky at arithmetic by default.
- **Truncation happens at token boundaries, not sentence boundaries.** A context-window overflow
  can silently cut a JSON payload mid-field. Always count tokens before you build the prompt, not
  after the API call fails.
- **Whitespace and casing are tokens too.** `" hello"` and `"hello"` are frequently different
  tokens. This is invisible until it isn't (weird few-shot formatting bugs trace back to this more
  often than you'd expect).

Rule of thumb for English: **~4 characters per token, ~¾ of a word per token.** Good enough for
back-of-envelope budgeting; use the vendor's actual tokenizer for anything that needs to be exact.

## Embeddings — meaning as geometry

An embedding model maps text (or images, audio) to a dense vector — typically 256 to 3072 floats
— such that **semantically similar inputs land close together** in that vector space. That single
property is the entire foundation of semantic search, RAG, deduplication, clustering, and
recommendation systems built on top of language models.

```python
embed("How do I reset my password?")   -> [0.021, -0.183, 0.402, ...]
embed("password recovery steps")        -> [0.019, -0.171, 0.395, ...]  # close vector — similar meaning
embed("best pizza toppings")            -> [0.512,  0.044, -0.220, ...] # far vector — unrelated
```

### Comparing vectors

```python
import numpy as np

def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))
```

Cosine similarity is the default because it ignores vector *magnitude* and only compares
*direction* — which matters because embedding magnitude is mostly an artifact of text length, not
meaning. Some vector databases default to dot product or Euclidean distance instead; check which
one your index uses, because mixing the wrong distance metric with the wrong embedding model gives
you confidently wrong search results with no error message.

### What actually differs between embedding models

| Factor | Why it matters |
|---|---|
| **Dimensionality** | Higher isn't always better — more storage/compute per vector, diminishing returns past a point for most use cases |
| **Training domain** | A model trained mostly on web text underperforms on legal, medical, or code-heavy corpora — domain-specific or fine-tuned embeddings win there |
| **Max input length** | Embedding models truncate silently past their limit — chunk *before* embedding, never rely on the API to do it for you |
| **Symmetric vs asymmetric** | Some models are tuned for query↔document search (short question, long passage) rather than sentence-to-sentence similarity — using the wrong one degrades retrieval quality in a way that's hard to diagnose from the outside |

### Beyond RAG

Embeddings aren't just a RAG detail:

- **Deduplication** — near-duplicate support tickets, log lines, or user feedback cluster together
  even with different wording.
- **Clustering / topic discovery** — k-means or HDBSCAN over embeddings surfaces themes in
  unstructured text without labels.
- **Semantic caching** — cache LLM responses keyed by embedding similarity instead of exact string
  match, so paraphrased repeat questions hit the cache too.
- **Anomaly detection** — an embedding far from every known cluster is often a genuinely novel
  input worth flagging (fraud, spam, out-of-scope requests).

## Where this connects in the library

- [`../02-rag-architectures/`](../02-rag-architectures/) — chunking and embedding strategy is the
  single biggest lever on RAG quality, more than which LLM you call afterward.
- [`ml-fundamentals.md`](ml-fundamentals.md) — embeddings are where classic ML and LLM engineering
  actually meet: a vector is just a feature representation learned by a neural net.
- [`../05-evaluation-observability/`](../05-evaluation-observability/) — "retrieval hit rate" as a
  metric only makes sense once you understand that similarity search is approximate by
  construction, not a database lookup.
