# RAG Assistant — Source Code

End-to-end flow:

```
query → embed → retrieve → assemble context → generate (with citations)
```

### Failure modes to handle
- Irrelevant retrieval
- Context-window overflow
- Hallucinated citations

### Cost-aware RAG
- Cache repeated retrieval queries.
- Prompt-cache the static RAG system-prompt portions.

### Tech choices (document your selection)
- Embedding model: Ollama `nomic-embed-text` / Bedrock Titan Embeddings
- Vector store: PGVector / ChromaDB
- LLM: [your choice]
