package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory vector store — Artifact 2 RAG assistant.
 *
 * Topic: Embeddings & Vector Search (TL row 8)
 *
 * Production swap: ChromaDB persistent client or PGVector. Interface stays the same.
 */
@Service
public class VectorStoreService {

    private final Map<String, double[]> vectors = new LinkedHashMap<>();
    private final Map<String, DocumentChunk> chunks = new LinkedHashMap<>();

    public synchronized void add(DocumentChunk chunk, List<Float> vector) {
        double[] arr = new double[vector.size()];
        for (int i = 0; i < vector.size(); i++) arr[i] = vector.get(i);
        vectors.put(chunk.getId(), arr);
        chunks.put(chunk.getId(), chunk);
    }

    public synchronized List<SearchResult> search(List<Float> queryVector, int topK) {
        if (vectors.isEmpty()) return List.of();
        double[] q = new double[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) q[i] = queryVector.get(i);

        List<Map.Entry<String, Double>> scored = new ArrayList<>();
        for (Map.Entry<String, double[]> e : vectors.entrySet()) {
            double[] v = e.getValue();
            double dot = 0, nq = 0, nv = 0;
            for (int i = 0; i < q.length; i++) {
                dot += q[i] * v[i];
                nq += q[i] * q[i];
                nv += v[i] * v[i];
            }
            double sim = (Math.sqrt(nq) * Math.sqrt(nv) == 0) ? 0 : dot / (Math.sqrt(nq) * Math.sqrt(nv));
            scored.add(Map.entry(e.getKey(), sim));
        }
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<SearchResult> results = new ArrayList<>();
        int rank = 0;
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            rank++;
            results.add(new SearchResult(chunks.get(scored.get(i).getKey()), scored.get(i).getValue(), rank));
        }
        return results;
    }

    public synchronized int size() { return vectors.size(); }
    public synchronized void clear() { vectors.clear(); chunks.clear(); }

    public static class SearchResult {
        public final DocumentChunk chunk;
        public double score;
        public int rank;
        public SearchResult(DocumentChunk chunk, double score, int rank) {
            this.chunk = chunk; this.score = score; this.rank = rank;
        }
    }
}
