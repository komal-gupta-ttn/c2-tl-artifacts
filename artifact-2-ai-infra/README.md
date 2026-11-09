# Artifact 2 — Shared AI Infrastructure (TL Differentiator)

> Build and run a piece of shared AI infrastructure for the project team:
> - **Option A:** RAG-backed assistant over docs/codebase
> - **Option B:** Deterministic multi-tool MCP chain (explicitly NOT autonomous looping/multi-agent)

Choose **one** option. Both satisfy the TL evidence requirement: *"A working RAG-backed tool or a chained multi-tool MCP workflow, built and demonstrated."*

## TL expectations addressed

1. Build and run shared AI infrastructure for the project team.
2. Be the go-to person when the project's AI tooling breaks or needs extending.
3. Review teammates' specs and AI-assisted output (this infra supports that).

---

## Option A — RAG-backed assistant

Location: `artifact-2-ai-infra/rag-assistant/`

### Topics covered (TL course block — ~20 hrs)
- **Embeddings & Vector Search:** dense vectors, cosine vs dot product; Ollama `nomic-embed-text` vs Bedrock Titan Embeddings; PGVector or ChromaDB setup & indexing; similarity-search tuning.
- **Chunking & Retrieval Pipeline Design:** fixed-size / paragraph-based / recursive-semantic chunking; metadata enrichment (source file, section, page); top-K, hybrid keyword+semantic, re-ranking.
- **RAG Architecture & Evaluation:** query → embed → retrieve → assemble → generate with source citations; failure modes (irrelevant retrieval, context overflow, hallucinated citations); lightweight eval harness (precision/recall, answer-accuracy on labeled set); cost-aware RAG (cache repeated queries, prompt-cache static system-prompt portions).

### Artefacts
- `docs/` — source documents/codebase indexed
- `index/` — vector store config (PGVector / ChromaDB)
- `src/` — RAG pipeline (embed → retrieve → assemble → generate)
- `eval/` — lightweight eval harness (labeled Q&A set, precision/recall scoring)

### Demo narrative
Show: ingest docs → query → grounded answer with citations → eval results.

---

## Option B — Deterministic multi-tool MCP chain

Location: `artifact-2-ai-infra/mcp-chain/`

### Topics covered (TL row 11 — 4.0 hrs)
- Build MCP client connecting to 2+ servers.
- Orchestrate a request across multiple tools in a **fixed sequence** (deterministic chaining — explicitly NOT autonomous looping/multi-agent).
- Error handling & fallback when a tool call fails mid-chain.
- Debug tool calls (inspect request/response, diagnose schema mismatches).

### Artefacts
- `servers/` — 2+ minimal MCP servers (Spring AI `@Tool` or FastMCP)
- `client/` — MCP client orchestrating fixed sequence
- `docs/` — tool schemas, chain definition, error-handling strategy

### Demo narrative
Show: client request → Tool A → Tool B → Tool C (fixed sequence) → result, with failure fallback.

---

## Decision

[ ] Option A — RAG assistant
[ ] Option B — MCP chain

## Demonstration

Be ready to demonstrate during Mentor review and talk through how it showed up in real project work.
