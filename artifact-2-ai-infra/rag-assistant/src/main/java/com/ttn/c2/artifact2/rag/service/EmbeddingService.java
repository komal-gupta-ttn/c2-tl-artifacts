package com.ttn.c2.artifact2.rag.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Embedding service — Artifact 2 RAG assistant.
 *
 * Topic: Embeddings & Vector Search (TL row 8)
 *
 * Supports Ollama nomic-embed-text / Bedrock Titan Embeddings in production.
 * For the demo, a deterministic pseudo-embedding is used so the artifact runs
 * without any external LLM key.
 */
@Service
public class EmbeddingService {

    private static final int DIM = 384;

    public List<Float> embed(String text) {
        if (text == null) text = "";
        // Deterministic pseudo-embedding seeded by text hash (stable, no external key)
        List<Float> vec = new ArrayList<>(DIM);
        long seed = text.hashCode();
        Random rng = new Random(seed);
        for (int i = 0; i < DIM; i++) {
            vec.add(rng.nextFloat());
        }
        // L2-normalize for cosine similarity
        double norm = 0;
        for (float v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < DIM; i++) vec.set(i, vec.get(i) / (float) norm);
        }
        return vec;
    }

    public List<List<Float>> embedBatch(List<String> texts) {
        List<List<Float>> out = new ArrayList<>(texts.size());
        for (String t : texts) out.add(embed(t));
        return out;
    }

    public double cosineSimilarity(List<Float> a, List<Float> b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            dot += a.get(i) * b.get(i);
            na += a.get(i) * a.get(i);
            nb += b.get(i) * b.get(i);
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0 : dot / denom;
    }

    public int getDim() { return DIM; }
}
