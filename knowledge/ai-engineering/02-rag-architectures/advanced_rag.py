"""Production-shaped hybrid RAG pipeline: dense vector search (Chroma) fused with
sparse keyword search (BM25) via Reciprocal Rank Fusion, then optionally reranked
with a cross-encoder before the top-k chunks reach the LLM.

Why hybrid instead of vector-only: pure semantic search misses exact-term queries
(error codes, product SKUs, person names) that keyword search nails and embeddings
smear across nearby-but-wrong vectors. See ../01-foundations/tokenization-and-embeddings.md
for why that happens, and rag-concepts.md in this folder for the retrieval-quality
tradeoffs referenced throughout this file.

Swap-out points, deliberately kept thin so this isn't married to one vendor:
  - Chroma -> Supabase (pgvector) or Qdrant: only VectorStore.__init__/upsert/search change.
  - OpenAI embeddings -> any embedding model: only the `embed()` call changes.
  - Anthropic/OpenAI completion -> Ollama: see ../04-local-inference/local_ollama_chat.py
    for the local-model version of the same "answer using only these sources" prompt.

Install: pip install -r ../requirements.txt
"""

from __future__ import annotations

import os
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Sequence

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import (  # noqa: E402
    LLMCallError,
    RetrievalError,
    estimate_tokens,
    get_logger,
    require_env,
    retry_with_backoff,
    timed,
)

logger = get_logger(__name__)

DEFAULT_CHUNK_SIZE = 800
DEFAULT_CHUNK_OVERLAP = 150
DEFAULT_TOP_K = 5
DEFAULT_FETCH_K = 20  # candidates pulled from each retriever before fusion + rerank


@dataclass
class Chunk:
    id: str
    text: str
    source: str
    metadata: dict = field(default_factory=dict)


@dataclass
class RetrievedChunk:
    chunk: Chunk
    score: float
    retriever: str  # "vector" | "keyword" | "fused" | "reranked"


def chunk_document(source: str, text: str, *, chunk_size: int = DEFAULT_CHUNK_SIZE,
                    overlap: int = DEFAULT_CHUNK_OVERLAP) -> list[Chunk]:
    """Split on paragraph boundaries first, only falling back to a hard character
    cut when a single paragraph exceeds chunk_size. Never split mid-sentence if a
    paragraph break is available nearby — garbage chunks are the #1 RAG failure mode
    per rag-concepts.md, and most of that garbage comes from naive fixed-width splitting.
    """
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    chunks: list[Chunk] = []
    buffer = ""
    for paragraph in paragraphs:
        candidate = f"{buffer}\n\n{paragraph}".strip() if buffer else paragraph
        if len(candidate) <= chunk_size:
            buffer = candidate
            continue

        if buffer:
            chunks.append(_make_chunk(source, buffer, len(chunks)))
        if len(paragraph) <= chunk_size:
            buffer = paragraph
        else:
            for i in range(0, len(paragraph), chunk_size - overlap):
                piece = paragraph[i : i + chunk_size]
                chunks.append(_make_chunk(source, piece, len(chunks)))
            buffer = ""

    if buffer:
        chunks.append(_make_chunk(source, buffer, len(chunks)))
    return chunks


def _make_chunk(source: str, text: str, index: int) -> Chunk:
    return Chunk(id=f"{source}::{index}", text=text, source=source, metadata={"chunk_index": index})


class VectorStore:
    """Thin wrapper around Chroma. Swap for a Supabase/pgvector client by reimplementing
    these three methods with the same signatures — nothing else in this file needs to change.
    """

    def __init__(self, collection_name: str = "advanced_rag_demo", persist_directory: str | None = None):
        try:
            import chromadb
            from chromadb.utils import embedding_functions
        except ImportError as exc:
            raise RetrievalError(
                "chromadb is not installed. Run: pip install chromadb"
            ) from exc

        client = (
            chromadb.PersistentClient(path=persist_directory)
            if persist_directory
            else chromadb.EphemeralClient()
        )
        embedder = embedding_functions.OpenAIEmbeddingFunction(
            api_key=require_env("OPENAI_API_KEY", default=os.environ.get("OPENAI_API_KEY", "")),
            model_name="text-embedding-3-small",
        )
        self._collection = client.get_or_create_collection(
            name=collection_name, embedding_function=embedder
        )

    def upsert(self, chunks: Sequence[Chunk]) -> None:
        if not chunks:
            return
        self._collection.upsert(
            ids=[c.id for c in chunks],
            documents=[c.text for c in chunks],
            metadatas=[{**c.metadata, "source": c.source} for c in chunks],
        )

    def search(self, query: str, top_k: int) -> list[RetrievedChunk]:
        result = self._collection.query(query_texts=[query], n_results=top_k)
        out: list[RetrievedChunk] = []
        for i, doc_id in enumerate(result["ids"][0]):
            distance = result["distances"][0][i]
            metadata = result["metadatas"][0][i]
            text = result["documents"][0][i]
            chunk = Chunk(id=doc_id, text=text, source=metadata.get("source", "unknown"), metadata=metadata)
            out.append(RetrievedChunk(chunk=chunk, score=1.0 - distance, retriever="vector"))
        return out


class KeywordIndex:
    """BM25 over the same chunk set. Kept in-process and rebuilt on upsert — fine up to
    tens of thousands of chunks; past that, move to an Elasticsearch/OpenSearch BM25 index
    instead of scaling this naive version.
    """

    def __init__(self) -> None:
        self._chunks: list[Chunk] = []
        self._bm25 = None

    def upsert(self, chunks: Sequence[Chunk]) -> None:
        self._chunks.extend(chunks)
        self._rebuild()

    def _rebuild(self) -> None:
        try:
            from rank_bm25 import BM25Okapi
        except ImportError as exc:
            raise RetrievalError("rank_bm25 is not installed. Run: pip install rank_bm25") from exc

        tokenized = [c.text.lower().split() for c in self._chunks]
        self._bm25 = BM25Okapi(tokenized) if tokenized else None

    def search(self, query: str, top_k: int) -> list[RetrievedChunk]:
        if self._bm25 is None:
            return []
        scores = self._bm25.get_scores(query.lower().split())
        ranked = sorted(zip(self._chunks, scores), key=lambda pair: pair[1], reverse=True)[:top_k]
        return [
            RetrievedChunk(chunk=chunk, score=float(score), retriever="keyword")
            for chunk, score in ranked
            if score > 0
        ]


def reciprocal_rank_fusion(
    ranked_lists: Sequence[list[RetrievedChunk]], *, k: int = 60
) -> list[RetrievedChunk]:
    """Combine multiple ranked lists into one without needing the scores to be on the
    same scale — vector cosine similarity and BM25 scores are not comparable numbers,
    which is exactly why naive score-averaging hybrid search is a common mistake.
    RRF only uses rank position: score = sum(1 / (k + rank)) across all lists a chunk
    appears in. k=60 is the value from the original RRF paper and works well in practice.
    """
    fused_scores: dict[str, float] = {}
    chunk_by_id: dict[str, Chunk] = {}

    for ranked_list in ranked_lists:
        for rank, item in enumerate(ranked_list):
            fused_scores[item.chunk.id] = fused_scores.get(item.chunk.id, 0.0) + 1.0 / (k + rank + 1)
            chunk_by_id[item.chunk.id] = item.chunk

    ordered_ids = sorted(fused_scores, key=lambda cid: fused_scores[cid], reverse=True)
    return [
        RetrievedChunk(chunk=chunk_by_id[cid], score=fused_scores[cid], retriever="fused")
        for cid in ordered_ids
    ]


def rerank(query: str, candidates: list[RetrievedChunk], top_k: int) -> list[RetrievedChunk]:
    """Cross-encoder rerank of the fused candidates. This is the cheapest big quality
    win in RAG per rag-concepts.md: a cross-encoder scores (query, chunk) pairs jointly
    instead of comparing precomputed independent vectors, catching relevance a bi-encoder
    embedding misses. Only worth running on ~20-30 candidates, not the whole corpus —
    it's much slower per-item than vector search by design.
    """
    if not candidates:
        return []
    try:
        from sentence_transformers import CrossEncoder
    except ImportError:
        logger.warning("sentence-transformers not installed, skipping rerank step — install it for better precision")
        return candidates[:top_k]

    model = CrossEncoder("cross-encoder/ms-marco-MiniLM-L-6-v2")
    pairs = [(query, c.chunk.text) for c in candidates]
    scores = model.predict(pairs)
    reranked = sorted(zip(candidates, scores), key=lambda pair: pair[1], reverse=True)[:top_k]
    return [
        RetrievedChunk(chunk=item.chunk, score=float(score), retriever="reranked")
        for item, score in reranked
    ]


@dataclass
class RAGAnswer:
    answer: str
    sources: list[Chunk]
    retrieval_debug: list[RetrievedChunk]


class AdvancedRAGPipeline:
    def __init__(self, collection_name: str = "advanced_rag_demo"):
        self.vector_store = VectorStore(collection_name=collection_name)
        self.keyword_index = KeywordIndex()

    def index_documents(self, documents: dict[str, str]) -> int:
        """documents: {source_name: raw_text}. Returns the number of chunks indexed."""
        all_chunks: list[Chunk] = []
        for source, text in documents.items():
            all_chunks.extend(chunk_document(source, text))

        with timed(f"indexing {len(all_chunks)} chunks", logger):
            self.vector_store.upsert(all_chunks)
            self.keyword_index.upsert(all_chunks)

        logger.info("indexed %d chunks from %d documents", len(all_chunks), len(documents))
        return len(all_chunks)

    def query(self, question: str, *, top_k: int = DEFAULT_TOP_K, fetch_k: int = DEFAULT_FETCH_K) -> RAGAnswer:
        with timed("hybrid retrieval", logger):
            vector_hits = self.vector_store.search(question, top_k=fetch_k)
            keyword_hits = self.keyword_index.search(question, top_k=fetch_k)
            fused = reciprocal_rank_fusion([vector_hits, keyword_hits])
            final = rerank(question, fused[:fetch_k], top_k=top_k)

        if not final:
            logger.warning("no chunks retrieved for query: %s", question)
            return RAGAnswer(
                answer="I don't have enough indexed context to answer that.",
                sources=[],
                retrieval_debug=[],
            )

        prompt = self._build_prompt(question, final)
        logger.debug("prompt is ~%d tokens", estimate_tokens(prompt))
        answer_text = self._call_llm(prompt)

        return RAGAnswer(
            answer=answer_text,
            sources=[hit.chunk for hit in final],
            retrieval_debug=final,
        )

    @staticmethod
    def _build_prompt(question: str, chunks: list[RetrievedChunk]) -> str:
        numbered_sources = "\n\n".join(
            f"[{i + 1}] (source: {hit.chunk.source})\n{hit.chunk.text}"
            for i, hit in enumerate(chunks)
        )
        return (
            "Answer the question using ONLY the sources below. Cite sources inline as "
            "[n]. If the sources don't contain the answer, say so explicitly instead of "
            "guessing.\n\n"
            f"Sources:\n{numbered_sources}\n\n"
            f"Question: {question}"
        )

    @retry_with_backoff(max_attempts=3, retryable_exceptions=(Exception,))
    def _call_llm(self, prompt: str) -> str:
        try:
            import anthropic
        except ImportError as exc:
            raise LLMCallError("anthropic SDK not installed. Run: pip install anthropic") from exc

        client = anthropic.Anthropic(api_key=require_env("ANTHROPIC_API_KEY"))
        response = client.messages.create(
            model="claude-sonnet-5",
            max_tokens=1024,
            system="You are a precise assistant that only answers from the provided sources.",
            messages=[{"role": "user", "content": prompt}],
        )
        return response.content[0].text


def _demo() -> None:
    """A tiny self-contained example. Real usage points index_documents() at your own
    corpus (docs, tickets, wikis) instead of these three toy paragraphs.
    """
    sample_docs = {
        "deploy-runbook.md": (
            "To roll back a bad deploy, run `kubectl rollout undo deployment/api`. "
            "This reverts to the previous ReplicaSet within roughly 30 seconds.\n\n"
            "Always check the rollout status with `kubectl rollout status` before "
            "declaring the incident resolved."
        ),
        "onboarding.md": (
            "New engineers get repo access via the platform team's Slack channel. "
            "Request access to the monorepo and the staging Kubernetes cluster on day one."
        ),
    }

    pipeline = AdvancedRAGPipeline()
    pipeline.index_documents(sample_docs)
    result = pipeline.query("How do I roll back a bad deployment?")

    print(f"\nAnswer:\n{result.answer}\n")
    print("Sources used:")
    for chunk in result.sources:
        print(f"  - {chunk.source} (chunk {chunk.metadata.get('chunk_index')})")


if __name__ == "__main__":
    _demo()
