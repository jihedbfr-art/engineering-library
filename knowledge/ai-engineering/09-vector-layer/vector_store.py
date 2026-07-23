"""A VectorStore interface with two backends: an in-memory one (numpy cosine
similarity, zero external deps) and a thin Chroma adapter over the same client
02-rag-architectures/advanced_rag.py already uses. See vector-store-abstraction.md
for why upsert/search/delete is the whole interface and what's deliberately left
unabstracted (filter syntax varies by backend, on purpose).

Install: pip install -r ../requirements.txt   (Chroma optional — falls back to
                                                in-memory if chromadb isn't installed)
Run:     python vector_store.py
"""

from __future__ import annotations

import sys
import uuid
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Protocol

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import RetrievalError, get_logger, timed  # noqa: E402

logger = get_logger(__name__)


@dataclass
class VectorRecord:
    id: str
    vector: list[float]
    text: str
    metadata: dict = field(default_factory=dict)


@dataclass
class VectorMatch:
    record: VectorRecord
    score: float  # cosine similarity, higher is closer, in [-1, 1]


class VectorBackend(Protocol):
    """Structural interface — a class satisfies this by having these three
    methods, no inheritance required. Chroma, pgvector, Qdrant adapters all
    implement the same shape; callers never branch on which one they got.
    """

    def upsert(self, records: list[VectorRecord]) -> None: ...
    def search(self, query_vector: list[float], top_k: int, filters: dict | None = None) -> list[VectorMatch]: ...
    def delete(self, ids: list[str]) -> None: ...


class InMemoryVectorStore:
    """Brute-force cosine similarity over a numpy array. O(n) per search — genuinely
    fine up to a few thousand vectors, which covers most demos, tests, and a
    surprising number of real small-corpus production use cases.
    """

    def __init__(self) -> None:
        self._records: dict[str, VectorRecord] = {}

    def upsert(self, records: list[VectorRecord]) -> None:
        for record in records:
            self._records[record.id] = record
        logger.info("in-memory store: upserted %d record(s), %d total", len(records), len(self._records))

    def search(self, query_vector: list[float], top_k: int, filters: dict | None = None) -> list[VectorMatch]:
        candidates = self._apply_filters(self._records.values(), filters)
        if not candidates:
            return []
        query = np.array(query_vector)
        query_norm = np.linalg.norm(query) or 1e-9
        scored = []
        for record in candidates:
            vec = np.array(record.vector)
            denom = (np.linalg.norm(vec) * query_norm) or 1e-9
            similarity = float(np.dot(vec, query) / denom)
            scored.append(VectorMatch(record=record, score=similarity))
        scored.sort(key=lambda m: m.score, reverse=True)
        return scored[:top_k]

    def delete(self, ids: list[str]) -> None:
        for record_id in ids:
            self._records.pop(record_id, None)

    @staticmethod
    def _apply_filters(records, filters: dict | None) -> list[VectorRecord]:
        if not filters:
            return list(records)
        return [r for r in records if all(r.metadata.get(k) == v for k, v in filters.items())]


class ChromaVectorStore:
    """Adapter over chromadb, same interface as InMemoryVectorStore. Metadata
    filters use Chroma's own `where` syntax once translated from the plain-dict
    filters this module accepts — that translation is the part that would need
    to change for a different backend's filter DSL.
    """

    def __init__(self, collection_name: str = "engineering-library-demo"):
        try:
            import chromadb
        except ImportError as exc:
            raise RetrievalError("chromadb not installed — pip install chromadb, or use InMemoryVectorStore") from exc
        self._client = chromadb.Client()
        self._collection = self._client.get_or_create_collection(collection_name)

    def upsert(self, records: list[VectorRecord]) -> None:
        self._collection.upsert(
            ids=[r.id for r in records],
            embeddings=[r.vector for r in records],
            documents=[r.text for r in records],
            metadatas=[r.metadata or {} for r in records],
        )
        logger.info("chroma store: upserted %d record(s)", len(records))

    def search(self, query_vector: list[float], top_k: int, filters: dict | None = None) -> list[VectorMatch]:
        result = self._collection.query(
            query_embeddings=[query_vector],
            n_results=top_k,
            where=filters or None,
        )
        matches = []
        ids = result["ids"][0]
        docs = result["documents"][0]
        metadatas = result["metadatas"][0]
        distances = result["distances"][0]  # Chroma default: squared L2, not cosine
        for id_, doc, meta, distance in zip(ids, docs, metadatas, distances):
            similarity = 1.0 / (1.0 + distance)  # rough monotonic stand-in, not a true cosine score
            matches.append(VectorMatch(
                record=VectorRecord(id=id_, vector=[], text=doc, metadata=meta or {}),
                score=similarity,
            ))
        return matches

    def delete(self, ids: list[str]) -> None:
        self._collection.delete(ids=ids)


class VectorStore:
    """Factory — the only place that decides which concrete backend a caller
    gets. Everything above this line is a backend implementation; everything
    that calls VectorStore.create() should never need to know which one it got.
    """

    @staticmethod
    def create(backend: str = "auto", **kwargs: Any) -> VectorBackend:
        if backend == "memory":
            return InMemoryVectorStore()
        if backend == "chroma":
            return ChromaVectorStore(**kwargs)
        if backend == "auto":
            try:
                return ChromaVectorStore(**kwargs)
            except RetrievalError:
                logger.info("chromadb not available, falling back to in-memory backend")
                return InMemoryVectorStore()
        raise RetrievalError(f"unknown backend '{backend}', expected 'memory', 'chroma', or 'auto'")


def _fake_embed(text: str, dims: int = 8) -> list[float]:
    """Deterministic pseudo-embedding for the demo — real code calls an actual
    embedding model, see ../01-foundations/tokenization-and-embeddings.md.
    """
    seed = sum(ord(c) for c in text)
    rng = np.random.default_rng(seed)
    return rng.normal(size=dims).tolist()


def _demo() -> None:
    store = VectorStore.create(backend="memory")

    docs = [
        ("doc-1", "Hybrid RAG fuses dense and sparse retrieval with RRF.", {"topic": "rag"}),
        ("doc-2", "Ollama runs local models with no per-token API cost.", {"topic": "local-inference"}),
        ("doc-3", "A hook can veto a tool call before it executes.", {"topic": "agents"}),
    ]
    records = [VectorRecord(id=id_, vector=_fake_embed(text), text=text, metadata=meta) for id_, text, meta in docs]

    with timed("upsert", logger):
        store.upsert(records)

    query_vector = _fake_embed("How does local model inference avoid API costs?")
    with timed("search", logger):
        matches = store.search(query_vector, top_k=2)

    print("Top matches (no metadata filter):")
    for m in matches:
        print(f"  [{m.score:+.3f}] {m.record.text}")

    filtered = store.search(query_vector, top_k=2, filters={"topic": "agents"})
    print("\nSame query, filtered to topic='agents':")
    for m in filtered:
        print(f"  [{m.score:+.3f}] {m.record.text}")

    store.delete(["doc-2"])
    print(f"\nafter delete: {len(store.search(query_vector, top_k=10))} record(s) remain")


if __name__ == "__main__":
    _demo()
