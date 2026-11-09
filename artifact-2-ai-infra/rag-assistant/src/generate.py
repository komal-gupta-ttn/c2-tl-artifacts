"""
Generation module — Artifact 2 RAG assistant.

Assembles retrieved context into a prompt, generates an answer with source
citations, and validates citations against retrieved docs.

Topic: RAG Architecture & Evaluation (TL row 10)
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from .retrieve import RetrievalResult


@dataclass
class GeneratedAnswer:
    answer: str
    citations: list[dict[str, Any]]
    context_used: list[str]
    grounded: bool


class Generator:
    """
    Generates an answer from retrieved chunks with citations.

    In production, this calls an LLM (Claude via Bedrock, or a local Ollama
    model) with a RAG system prompt. Here a deterministic template is used so
    the artifact runs without an LLM key.
    """

    def __init__(self, model: str = "template-fallback"):
        self.model = model

    def generate(
        self, query: str, results: list[RetrievalResult]
    ) -> GeneratedAnswer:
        if not results:
            return GeneratedAnswer(
                answer="I couldn't find any relevant documentation for that question. "
                "Try rephrasing or check that the docs have been indexed.",
                citations=[],
                context_used=[],
                grounded=True,
            )

        # Build context from top results
        context_parts: list[str] = []
        citations: list[dict[str, Any]] = []
        for r in results:
            ctx = f"[{r.chunk.source_file}#{r.chunk.chunk_index}] {r.chunk.text}"
            context_parts.append(ctx)
            citations.append(
                {
                    "source_file": r.chunk.source_file,
                    "chunk_id": r.chunk.id,
                    "section": r.chunk.section,
                    "score": round(r.score, 4),
                    "snippet": r.chunk.text[:200],
                }
            )

        context = "\n\n---\n\n".join(context_parts)

        # Deterministic answer (template). Replace with LLM call in production:
        #   llm.generate(system_prompt + context + query)
        answer = self._render_answer(query, context, citations)

        grounded = all(c["source_file"] for c in citations)

        return GeneratedAnswer(
            answer=answer,
            citations=citations,
            context_used=[c["chunk_id"] for c in citations],
            grounded=grounded,
        )

    def _render_answer(
        self, query: str, context: str, citations: list[dict[str, Any]]
    ) -> str:
        lines: list[str] = []
        lines.append(f"## Answer")
        lines.append("")
        lines.append(
            "Based on the indexed documentation, here's what I found for your question."
        )
        lines.append("")
        lines.append("### Supporting evidence")
        for c in citations:
            lines.append(
                f"- **{c['source_file']}** (score {c['score']}): {c['snippet']}…"
            )
        lines.append("")
        lines.append("### Sources")
        for c in citations:
            sec = f" · section: {c['section']}" if c["section"] else ""
            lines.append(f"- {c['source_file']}#{c['chunk_id'].split(':')[-1]}{sec}")
        lines.append("")
        lines.append(
            "_Note: This is a template-generated answer. In production, replace with "
            "an LLM call using the retrieved context and system prompt._"
        )
        return "\n".join(lines)


class CitationValidator:
    """
    Validates that every cited source actually exists in the retrieved set
    (catches hallucinated citations).
    """

    def __init__(self, results: list[RetrievalResult]):
        self.valid_ids = {r.chunk.id for r in results}

    def validate(self, cited_ids: list[str]) -> tuple[bool, list[str]]:
        invalid = [cid for cid in cited_ids if cid not in self.valid_ids]
        return len(invalid) == 0, invalid
