"""
Chunking module — Artifact 2 RAG assistant.

Strategies: fixed-size, paragraph-based, recursive/semantic.
Metadata enrichment: source file, section, page.

Topic: Chunking & Retrieval Pipeline Design (TL rows 8-9)
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from typing import Any


@dataclass
class Chunk:
    id: str
    text: str
    source_file: str
    section: str | None = None
    page: int | None = None
    chunk_index: int = 0
    token_count: int = 0
    metadata: dict[str, Any] = field(default_factory=dict)


def chunk_fixed_size(
    text: str, source_file: str, chunk_size: int = 500, overlap: int = 50
) -> list[Chunk]:
    """Split into fixed-size chunks with overlap."""
    words = text.split()
    chunks: list[Chunk] = []
    i = 0
    idx = 0
    while i < len(words):
        chunk_words = words[i : i + chunk_size]
        chunk_text = " ".join(chunk_words)
        chunks.append(
            Chunk(
                id=f"{source_file}:{idx}",
                text=chunk_text,
                source_file=source_file,
                chunk_index=idx,
                token_count=len(chunk_words),
            )
        )
        idx += 1
        i += chunk_size - overlap
    return chunks


def chunk_paragraphs(
    text: str, source_file: str, max_chunk_size: int = 800
) -> list[Chunk]:
    """Split on paragraph boundaries, respecting max size."""
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    chunks: list[Chunk] = []
    idx = 0
    for para in paragraphs:
        if len(para.split()) <= max_chunk_size:
            chunks.append(
                Chunk(
                    id=f"{source_file}:{idx}",
                    text=para,
                    source_file=source_file,
                    section=_infer_section(para),
                    chunk_index=idx,
                    token_count=len(para.split()),
                )
            )
            idx += 1
        else:
            # Paragraph too large — fall back to fixed-size within it
            sub = chunk_fixed_size(para, source_file, max_chunk_size, 50)
            for c in sub:
                c.id = f"{source_file}:{idx}"
                c.chunk_index = idx
                chunks.append(c)
                idx += 1
    return chunks


def chunk_recursive_semantic(
    text: str, source_file: str, max_chunk_size: int = 600
) -> list[Chunk]:
    """
    Recursive/semantic chunking: split on headers first, then paragraphs,
    then sentences — keeping related content together.
    """
    # Split on markdown-style headers
    sections = re.split(r"(?m)^(#{1,6}\s+.+)$", text)
    chunks: list[Chunk] = []
    idx = 0
    current_section: str | None = None
    for part in sections:
        part = part.strip()
        if not part:
            continue
        if re.match(r"^#{1,6}\s+", part):
            current_section = part
            continue
        # Within a section, split into sentences then group
        sentences = re.split(r"(?<=[.!?])\s+", part)
        current_chunk: list[str] = []
        current_len = 0
        for sent in sentences:
            sent_words = len(sent.split())
            if current_len + sent_words > max_chunk_size and current_chunk:
                chunks.append(
                    Chunk(
                        id=f"{source_file}:{idx}",
                        text=" ".join(current_chunk),
                        source_file=source_file,
                        section=current_section,
                        chunk_index=idx,
                        token_count=current_len,
                    )
                )
                idx += 1
                current_chunk = []
                current_len = 0
            current_chunk.append(sent)
            current_len += sent_words
        if current_chunk:
            chunks.append(
                Chunk(
                    id=f"{source_file}:{idx}",
                    text=" ".join(current_chunk),
                    source_file=source_file,
                    section=current_section,
                    chunk_index=idx,
                    token_count=current_len,
                )
            )
            idx += 1
    return chunks


def _infer_section(text: str) -> str | None:
    first_line = text.strip().split("\n")[0]
    if first_line.startswith("#"):
        return first_line.lstrip("#").strip()
    return None
