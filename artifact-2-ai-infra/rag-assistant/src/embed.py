"""
Embedding module — Artifact 2 RAG assistant.

Supports:
  - Ollama nomic-embed-text (local, zero cost, private)
  - Bedrock Titan Embeddings (cloud, managed)
  - Deterministic hash-based fallback (no external key required — for demo/dev)

Topic: Embeddings & Vector Search (TL row 8)
"""

from __future__ import annotations

import hashlib
import os
from typing import Any

import numpy as np


class Embedder:
    """
    Embeds text into dense vectors.

    Defaults to a deterministic hash-based embedding so the artifact is runnable
    without an LLM key. Swap `_ollama_embed` / `_bedrock_embed` in for real use.
    """

    def __init__(self, model: str = "hash-fallback", dim: int = 384):
        self.model = model
        self.dim = dim
        self._ollama_host = os.getenv("OLLAMA_HOST", "http://localhost:11434")
        self._bedrock_region = os.getenv("AWS_REGION", "us-east-1")

    def embed(self, text: str) -> list[float]:
        if self.model == "hash-fallback":
            return self._hash_embed(text)
        if self.model.startswith("nomic-embed-text"):
            return self._ollama_embed(text)
        if self.model.startswith("titan"):
            return self._bedrock_embed(text)
        raise ValueError(f"Unknown embedding model: {self.model}")

    def embed_batch(self, texts: list[str]) -> list[list[float]]:
        return [self.embed(t) for t in texts]

    # ---- implementations ----

    def _hash_embed(self, text: str) -> list[float]:
        """
        Deterministic pseudo-embedding from text hashes.

        Not semantically meaningful, but stable and sufficient to demo the
        embed → retrieve → assemble → generate flow without external deps.
        """
        vec = np.zeros(self.dim, dtype=np.float32)
        # Use multiple hash seeds to spread across dimensions
        for i in range(self.dim // 4):
            seed = f"{text}:{i}"
            h = hashlib.sha256(seed.encode()).digest()
            val = int.from_bytes(h[:4], "little") / (2**32)
            vec[i * 4 : i * 4 + 4] = [val] * 4
        # Normalize to unit length (cosine-space ready)
        norm = np.linalg.norm(vec)
        if norm > 0:
            vec = vec / norm
        return vec.tolist()

    def _ollama_embed(self, text: str) -> list[float]:
        """Ollama nomic-embed-text via REST API."""
        import json
        import urllib.request

        payload = json.dumps({"model": "nomic-embed-text", "prompt": text}).encode()
        req = urllib.request.Request(
            f"{self._ollama_host}/api/embeddings",
            data=payload,
            headers={"Content-Type": "application/json"},
        )
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = json.loads(resp.read())
        return data["embedding"]

    def _bedrock_embed(self, text: str) -> list[float]:
        """Bedrock Titan Embeddings via boto3."""
        import boto3

        client = boto3.client("bedrock-runtime", region_name=self._bedrock_region)
        resp = client.invoke_model(
            modelId="amazon.titan-embed-text-v1",
            body=__import__("json").dumps({"inputText": text}),
        )
        import json as _json

        return _json.loads(resp["body"].read())["embedding"]

    def cosine_similarity(self, a: list[float], b: list[float]) -> float:
        va = np.array(a, dtype=np.float32)
        vb = np.array(b, dtype=np.float32)
        denom = np.linalg.norm(va) * np.linalg.norm(vb)
        if denom == 0:
            return 0.0
        return float(np.dot(va, vb) / denom)
