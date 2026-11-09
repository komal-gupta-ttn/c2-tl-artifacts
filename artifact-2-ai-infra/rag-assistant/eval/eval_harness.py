"""
Evaluation harness — Artifact 2 RAG assistant.

Lightweight eval per C2 TL requirements:
  - Labeled Q&A set (question → expected answer → source doc)
  - Retrieval precision/recall
  - Answer-accuracy scoring
  - Citation correctness check

Topic: RAG Architecture & Evaluation (TL row 10)
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

# Allow running as: python -m eval.eval_harness
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.chunk import chunk_recursive_semantic
from src.embed import Embedder
from src.generate import Generator
from src.retrieve import Retriever, build_store_from_chunks


def load_labeled_qa(path: Path) -> list[dict]:
    qa: list[dict] = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                qa.append(json.loads(line))
    return qa


def run_eval(
    docs_dir: str | Path,
    qa_path: str | Path,
    top_k: int = 5,
    min_similarity: float = 0.3,
) -> dict:
    docs_dir = Path(docs_dir)
    qa_path = Path(qa_path)

    # Load docs + build index
    docs: list[tuple[str, str]] = []
    for f in sorted(docs_dir.rglob("*")):
        if f.is_file() and f.suffix.lower() in (".md", ".txt"):
            docs.append((str(f.relative_to(docs_dir)), f.read_text(encoding="utf-8")))

    embedder = Embedder()
    all_chunks: list = []
    for source_file, text in docs:
        chunks = chunk_recursive_semantic(text, source_file)
        all_chunks.extend(chunks)

    store = build_store_from_chunks(all_chunks, embedder)
    retriever = Retriever(store, embedder)
    generator = Generator()

    labeled_qa = load_labeled_qa(qa_path)

    total = len(labeled_qa)
    retrieval_hits = 0
    answer_correct = 0
    citation_valid = 0
    precision_scores: list[float] = []
    recall_scores: list[float] = []

    for item in labeled_qa:
        query = item["question"]
        expected_source = item.get("expected_source", "")
        expected_keywords = item.get("expected_keywords", [])

        # Retrieve
        results = retriever.retrieve(query, top_k=top_k, min_similarity=min_similarity)

        # Retrieval hit: expected source appears in top-K
        retrieved_sources = [r.chunk.source_file for r in results]
        if expected_source and expected_source in retrieved_sources:
            retrieval_hits += 1

        # Precision@K: fraction of retrieved chunks that are relevant (contain expected keyword)
        if results:
            relevant = sum(
                1
                for r in results
                if any(kw.lower() in r.chunk.text.lower() for kw in expected_keywords)
            )
            precision_scores.append(relevant / len(results))
            # Recall: fraction of expected relevant docs retrieved
            expected_docs = set(
                kw.lower() for kw in expected_keywords if any(
                    kw.lower() in r.chunk.text.lower() for r in results
                )
            )
            # Simplified: recall = 1 if expected source found
            recall_scores.append(1.0 if expected_source in retrieved_sources else 0.0)

        # Answer accuracy
        answer = generator.generate(query, results)
        answer_text = answer.answer.lower()
        keyword_hits = sum(1 for kw in expected_keywords if kw.lower() in answer_text)
        if expected_keywords and keyword_hits / len(expected_keywords) >= 0.5:
            answer_correct += 1

        # Citation validity
        cited_ids = [c["chunk_id"] for c in answer.citations]
        if cited_ids:
            valid_ids = {r.chunk.id for r in results}
            if all(cid in valid_ids for cid in cited_ids):
                citation_valid += 1

    metrics = {
        "total_questions": total,
        "retrieval_hit_rate": round(retrieval_hits / total, 3) if total else 0.0,
        "answer_accuracy": round(answer_correct / total, 3) if total else 0.0,
        "citation_validity": round(citation_valid / total, 3) if total else 0.0,
        "avg_precision_at_k": round(sum(precision_scores) / len(precision_scores), 3) if precision_scores else 0.0,
        "avg_recall_at_k": round(sum(recall_scores) / len(recall_scores), 3) if recall_scores else 0.0,
    }
    return metrics


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser(description="RAG eval harness")
    parser.add_argument("--docs", default="docs/sample_docs", help="Docs directory")
    parser.add_argument("--qa", default="eval/labeled_qa.jsonl", help="Labeled Q&A file")
    parser.add_argument("--top-k", type=int, default=5)
    parser.add_argument("--min-sim", type=float, default=0.3)
    args = parser.parse_args()

    metrics = run_eval(args.docs, args.qa, args.top_k, args.min_sim)
    print("\n=== RAG Evaluation Results ===")
    for k, v in metrics.items():
        print(f"  {k}: {v}")
    print()


if __name__ == "__main__":
    main()
