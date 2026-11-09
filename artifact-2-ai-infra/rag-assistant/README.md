# Artifact 2 — RAG-Backed Assistant (Option A)

> TL differentiator: build and run shared AI infrastructure for the project team.
> Evidence: "A working RAG-backed tool ... built and demonstrated."

## What it does

Ingests project docs/codebase → indexes into a vector store → answers natural-language
questions with grounded answers + source citations → evaluated with a lightweight
harness (precision/recall, answer-accuracy).

## Topics covered (TL course block — ~20 hrs)

| Topic | Where |
|-------|-------|
| Embeddings & Vector Search | `src/embed.py`, `index/vector_store_config.yaml` |
| Chunking & Retrieval Pipeline | `src/chunk.py`, `src/retrieve.py` |
| RAG Architecture & Evaluation | `src/rag_pipeline.py`, `eval/eval_harness.py` |

## Directory layout

```
rag-assistant/
├── README.md                   # this file
├── docs/
│   └── sample_docs/            # source documents to index
├── index/
│   └── vector_store_config.yaml  # PGVector / ChromaDB config
├── src/
│   ├── __init__.py
│   ├── rag_pipeline.py         # query → embed → retrieve → assemble → generate
│   ├── embed.py                # embedding (Ollama nomic-embed-text / Bedrock Titan)
│   ├── chunk.py                # chunking strategies + metadata enrichment
│   ├── retrieve.py             # vector search, top-K, hybrid, re-ranking
│   └── generate.py             # answer generation with citations
├── eval/
│   ├── eval_harness.py         # labeled Q&A, precision/recall, answer-accuracy
│   └── labeled_qa.jsonl        # labeled evaluation set
└── requirements.txt
```

## How to run

```bash
cd artifact-2-ai-infra/rag-assistant
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# Index sample docs
python -m src.rag_pipeline --index docs/sample_docs/

# Ask a question
python -m src.rag_pipeline --ask "How does the auth service handle token refresh?"

# Run evaluation
python -m eval.eval_harness
```

## Tech choices (documented)

| Component | Choice | Rationale |
|-----------|--------|-----------|
| Embedding model | Ollama `nomic-embed-text` (local) / Bedrock Titan Embeddings (cloud) | Local = zero cost, private; Bedrock = managed, scales |
| Vector store | ChromaDB (local dev) / PGVector (production) | ChromaDB zero-setup; PGVector keeps vectors alongside operational data |
| Chunking | Recursive/semantic with metadata enrichment | Preserves section boundaries + source file/page for filtered retrieval |
| Retrieval | top-K + hybrid (keyword + semantic) + re-ranking | Balances recall and precision |
| LLM | [your choice, e.g. Claude/Sonnet via Bedrock or Ollama] | Citation-capable model |

## Demo narrative (for Mentor review)

1. Show `docs/sample_docs/` — the project docs/codebase being indexed.
2. Run indexing: `python -m src.rag_pipeline --index docs/sample_docs/`.
3. Ask: "How does the auth service handle token refresh?" → answer with citations.
4. Show source citations pointing to actual doc lines (grounded, no hallucination).
5. Run `python -m eval.eval_harness` → precision/recall + answer-accuracy scorecard.
6. Explain cost-aware RAG: repeated queries cached; static system-prompt portions prompt-cached.
7. Explain how this shows up in real project work: team uses it to answer "how do we..." questions without interrupting senior engineers.

## Failure modes handled

- Irrelevant retrieval → low similarity threshold, return "no relevant docs found"
- Context-window overflow → truncate retrieved chunks to fit context budget
- Hallucinated citations → citation validator checks each cited doc exists
