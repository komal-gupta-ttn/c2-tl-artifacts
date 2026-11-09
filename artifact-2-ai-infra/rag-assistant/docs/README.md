# RAG Assistant — Source Documents

Place the docs/codebase to be indexed here.

## Ingestion pipeline

1. Load documents from this folder.
2. Chunk (fixed-size / paragraph-based / recursive-semantic).
3. Enrich metadata (source file, section, page).
4. Embed (Ollama `nomic-embed-text` or Bedrock Titan Embeddings).
5. Index into vector store (PGVector or ChromaDB).

## Retrieval tuning

- top-K selection
- hybrid keyword + semantic search
- re-ranking
- metadata-filtered retrieval

## Evaluation

Labeled Q&A set + precision/recall + answer-accuracy scoring in `eval/`.
