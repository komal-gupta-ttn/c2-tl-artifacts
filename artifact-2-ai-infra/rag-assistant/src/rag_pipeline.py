"""
RAG pipeline — Artifact 2 RAG assistant.

End-to-end flow:
  query → embed → retrieve → assemble context → generate (with citations)

CLI:
  python -m src.rag_pipeline --index docs/sample_docs/
  python -m src.rag_pipeline --ask "How does auth handle token refresh?"
  python -m src.rag_pipeline --eval
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .chunk import chunk_recursive_semantic
from .embed import Embedder
from .generate import Generator
from .retrieve import Retriever, build_store_from_chunks


def load_docs(docs_dir: Path) -> list[tuple[str, str]]:
    """Load all .md/.txt files from docs_dir → [(source_file, text), ...]."""
    docs: list[tuple[str, str]] = []
    for f in sorted(docs_dir.rglob("*")):
        if f.is_file() and f.suffix.lower() in (".md", ".txt", ".py"):
            rel = f.relative_to(docs_dir)
            docs.append((str(rel), f.read_text(encoding="utf-8")))
    return docs


def index_docs(docs_dir: str | Path, embedder: Embedder | None = None) -> int:
    docs_dir = Path(docs_dir)
    embedder = embedder or Embedder()
    docs = load_docs(docs_dir)

    all_chunks: list = []
    for source_file, text in docs:
        chunks = chunk_recursive_semantic(text, source_file)
        for c in chunks:
            c.metadata["indexed_from"] = str(docs_dir)
        all_chunks.extend(chunks)

    store = build_store_from_chunks(all_chunks, embedder)
    # Persist the store + embedder on the module for reuse by ask()
    _STORE["store"] = store
    _STORE["embedder"] = embedder
    print(f"Indexed {len(docs)} docs → {len(all_chunks)} chunks")
    return len(all_chunks)


_STORE: dict = {}


def ask(
    query: str,
    top_k: int = 5,
    min_similarity: float = 0.3,
    embedder: Embedder | None = None,
) -> dict:
    if "store" not in _STORE:
        raise RuntimeError("No index loaded. Run --index first.")
    store: Any = _STORE["store"]
    embedder = embedder or _STORE.get("embedder") or Embedder()
    retriever = Retriever(store, embedder)
    generator = Generator()

    results = retriever.retrieve(
        query, top_k=top_k, min_similarity=min_similarity
    )
    answer = generator.generate(query, results)

    return {
        "query": query,
        "answer": answer.answer,
        "grounded": answer.grounded,
        "citations": answer.citations,
        "num_results": len(results),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="RAG Assistant (C2 TL artifact)")
    parser.add_argument("--index", metavar="DIR", help="Index docs from directory")
    parser.add_argument("--ask", metavar="QUESTION", help="Ask a question")
    parser.add_argument("--top-k", type=int, default=5, help="Top-K results")
    parser.add_argument(
        "--min-sim", type=float, default=0.3, help="Minimum similarity threshold"
    )
    parser.add_argument(
        "--model", default="hash-fallback", help="Embedding model (hash-fallback default)"
    )
    args = parser.parse_args()

    embedder = Embedder(model=args.model)

    if args.index:
        index_docs(args.index, embedder)
        return

    if args.ask:
        # Ensure an index is loaded (auto-index default docs if needed)
        if "store" not in _STORE:
            default_docs = Path("docs/sample_docs")
            if default_docs.exists():
                index_docs(default_docs, embedder)
            else:
                raise RuntimeError("No index loaded. Run --index first.")
        result = ask(
            args.ask,
            top_k=args.top_k,
            min_similarity=args.min_sim,
            embedder=embedder,
        )
        print(result["answer"])
        print("\n--- Citations ---")
        for c in result["citations"]:
            print(f"  [{c['chunk_id']}] {c['source_file']} — score {c['score']}")
        return

    parser.print_help()


if __name__ == "__main__":
    main()
