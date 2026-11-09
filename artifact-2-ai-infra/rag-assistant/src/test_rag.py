"""
Tests for the RAG pipeline (Artifact 2).

Runs without external LLM keys using hash-fallback embeddings + in-memory store.
"""

import os
import sys
import tempfile

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from src.embed import Embedder
from src.chunk import chunk_recursive_semantic, chunk_fixed_size, chunk_paragraphs
from src.retrieve import VectorStore, Retriever, build_store_from_chunks
from src.generate import Generator, CitationValidator
from src.rag_pipeline import index_docs, ask, load_docs


def test_embedder_deterministic():
    e = Embedder(model="hash-fallback")
    v1 = e.embed("hello world")
    v2 = e.embed("hello world")
    assert v1 == v2
    assert len(v1) == 384


def test_embedder_cosine_similarity():
    e = Embedder(model="hash-fallback")
    a = e.embed("the quick brown fox")
    b = e.embed("the quick brown fox")
    c = e.embed("completely different topic about quantum physics")
    assert e.cosine_similarity(a, b) > e.cosine_similarity(a, c)


def test_chunk_recursive_semantic():
    text = "# Header A\nSome content here about auth.\n\n# Header B\nMore content about payments."
    chunks = chunk_recursive_semantic(text, "test.md")
    assert len(chunks) >= 1
    assert all(c.source_file == "test.md" for c in chunks)


def test_vector_store_search():
    e = Embedder(model="hash-fallback")
    store = VectorStore(e)
    from src.chunk import Chunk

    c1 = Chunk(id="d:0", text="auth token refresh flow", source_file="auth.md")
    c2 = Chunk(id="d:1", text="payments rate limit configuration", source_file="payments.md")
    store.add(c1, e.embed("auth token refresh flow"))
    store.add(c2, e.embed("payments rate limit configuration"))

    results = store.search(e.embed("how does token refresh work"), top_k=2)
    assert len(results) == 2
    # With hash-fallback (non-semantic) embeddings, just verify ordering by score desc
    assert results[0].score >= results[1].score


def test_retriever_hybrid_boost():
    e = Embedder(model="hash-fallback")
    store = VectorStore(e)
    from src.chunk import Chunk

    chunks = [
        Chunk(id="d:0", text="auth token refresh flow uses rotation", source_file="auth.md"),
        Chunk(id="d:1", text="unrelated topic about weather", source_file="weather.md"),
    ]
    store = build_store_from_chunks(chunks, e)
    retriever = Retriever(store, e)

    results = retriever.retrieve("auth token refresh", top_k=2)
    assert len(results) == 2
    assert results[0].chunk.source_file == "auth.md"


def test_generator_grounded():
    e = Embedder(model="hash-fallback")
    from src.chunk import Chunk
    from src.retrieve import RetrievalResult

    c = Chunk(id="d:0", text="auth uses refresh token rotation", source_file="auth.md")
    results = [RetrievalResult(chunk=c, score=0.9, rank=1)]
    gen = Generator()
    ans = gen.generate("how does auth refresh work", results)
    assert ans.grounded is True
    assert len(ans.citations) == 1
    assert ans.citations[0]["source_file"] == "auth.md"


def test_citation_validator():
    e = Embedder(model="hash-fallback")
    from src.chunk import Chunk
    from src.retrieve import RetrievalResult

    c = Chunk(id="d:0", text="content", source_file="x.md")
    results = [RetrievalResult(chunk=c, score=0.9, rank=1)]
    validator = CitationValidator(results)
    valid, invalid = validator.validate(["d:0"])
    assert valid is True
    assert invalid == []

    valid, invalid = validator.validate(["d:99"])
    assert valid is False
    assert invalid == ["d:99"]


def test_index_docs(tmp_path):
    # Create temp docs
    d = tmp_path / "docs"
    d.mkdir()
    (d / "auth.md").write_text("# Auth\nToken refresh rotates tokens.")
    (d / "pay.md").write_text("# Payments\nRate limit is 100 req/min.")

    count = index_docs(d)
    assert count == 2

    result = ask("how does token refresh work", top_k=3, embedder=Embedder())
    assert result["grounded"] is True
    assert len(result["citations"]) > 0
    assert any(c["source_file"] == "auth.md" for c in result["citations"])


def test_ask_no_index_raises():
    # Fresh process state: no store loaded
    import src.rag_pipeline as rp
    rp._STORE.clear()
    try:
        rp.ask("anything")
        assert False, "should have raised"
    except RuntimeError:
        pass


if __name__ == "__main__":
    import pytest
    sys.exit(pytest.main([__file__, "-v"]))
