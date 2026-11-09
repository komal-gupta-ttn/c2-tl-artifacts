"""
Retrieval module — Artifact 2 RAG assistant.

Vector store (ChromaDB local / PGVector production), similarity search,
hybrid keyword+semantic, re-ranking, metadata filtering.

Topic: Embeddings & Vector Search / Chunking & Retrieval (TL rows 8-9)
"""

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any

import numpy as np

from .chunk import Chunk
from .embed import Embedder


@dataclass
class RetrievalResult:
    chunk: Chunk
    score: float
    rank: int


class VectorStore:
    """
    In-memory vector store with ChromaDB hook.

    For production, replace `_index`/`_search` with ChromaDB persistent client
    or PGVector. The interface stays the same.
    """

    def __init__(self, embedder: Embedder):
        self.embedder = embedder
        self._vectors: dict[str, np.ndarray] = {}
        self._chunks: dict[str, Chunk] = {}

    def add(self, chunk: Chunk, vector: list[float]) -> None:
        self._vectors[chunk.id] = np.array(vector, dtype=np.float32)
        self._chunks[chunk.id] = chunk

    def search(self, query_vector: list[float], top_k: int = 5) -> list[RetrievalResult]:
        if not self._vectors:
            return []
        q = np.array(query_vector, dtype=np.float32)
        scores: dict[str, float] = {}
        for cid, vec in self._vectors.items():
            scores[cid] = float(np.dot(q, vec) / (np.linalg.norm(q) * np.linalg.norm(vec)))
        ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:top_k]
        results: list[RetrievalResult] = []
        for rank, (cid, score) in enumerate(ranked, start=1):
            results.append(RetrievalResult(chunk=self._chunks[cid], score=score, rank=rank))
        return results


class Retriever:
    """
    Orchestrates retrieval: embed query → vector search → optional hybrid → re-rank.
    """

    def __init__(self, store: VectorStore, embedder: Embedder):
        self.store = store
        self.embedder = embedder

    def retrieve(
        self,
        query: str,
        top_k: int = 5,
        min_similarity: float = 0.3,
        use_hybrid: bool = True,
    ) -> list[RetrievalResult]:
        qvec = self.embedder.embed(query)
        results = self.store.search(qvec, top_k=top_k * 2)  # over-fetch for re-ranking

        # Filter by similarity threshold
        results = [r for r in results if r.score >= min_similarity]

        # Optional hybrid: boost chunks containing query keywords
        if use_hybrid:
            results = self._hybrid_boost(query, results)

        # Re-rank: similarity + keyword overlap
        results = self._rerank(query, results)

        return results[:top_k]

    def _hybrid_boost(
        self, query: str, results: list[RetrievalResult]
    ) -> list[RetrievalResult]:
        qterms = set(query.lower().split())
        boosted: list[RetrievalResult] = []
        for r in results:
            cterms = set(r.chunk.text.lower().split())
            overlap = len(qterms & cterms) / max(len(qterms), 1)
            r.score = r.score + 0.1 * overlap  # small keyword boost
            boosted.append(r)
        boosted.sort(key=lambda x: x.score, reverse=True)
        return boosted

    def _rerank(self, query: str, results: list[RetrievalResult]) -> list[RetrievalResult]:
        """
        Simple re-ranking: prefer chunks whose section matches query intent,
        and penalize very short chunks.
        """
        qterms = set(query.lower().split())
        reranked: list[RetrievalResult] = []
        for r in results:
            score = r.score
            # Section match boost
            if r.chunk.section:
                sterms = set(r.chunk.section.lower().split())
                score += 0.05 * len(qterms & sterms)
            # Length penalty (very short chunks less useful)
            if r.chunk.token_count < 20:
                score -= 0.1
            reranked.append(
                RetrievalResult(chunk=r.chunk, score=score, rank=r.rank)
            )
        reranked.sort(key=lambda x: x.score, reverse=True)
        for i, r in enumerate(reranked, start=1):
            r.rank = i
        return reranked


def build_store_from_chunks(
    chunks: list[Chunk], embedder: Embedder
) -> VectorStore:
    store = VectorStore(embedder)
    vectors = embedder.embed_batch([c.text for c in chunks])
    for chunk, vec in zip(chunks, vectors):
        store.add(chunk, vec)
    return store
