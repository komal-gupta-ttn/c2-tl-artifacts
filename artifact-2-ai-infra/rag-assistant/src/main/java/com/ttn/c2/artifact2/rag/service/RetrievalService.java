package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieval service — Artifact 2 RAG assistant.
 *
 * Topics: Embeddings & Vector Search / Chunking & Retrieval (TL rows 8-9)
 *
 * Pipeline: embed query → vector search → hybrid keyword boost → re-rank → return top-K.
 */
@Service
public class RetrievalService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStore;

    public RetrievalService(EmbeddingService embeddingService, VectorStoreService vectorStore) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
    }

    public List<VectorStoreService.SearchResult> retrieve(String query, int topK, double minSimilarity,
                                                          boolean hybrid, boolean rerank) {
        if (query == null) return List.of();
        List<Float> qvec = embeddingService.embed(query);
        List<VectorStoreService.SearchResult> results = vectorStore.search(qvec, topK * 2);

        // Similarity threshold filter
        results.removeIf(r -> r.score < minSimilarity);

        // Hybrid: keyword overlap boost
        if (hybrid) {
            Set<String> qTerms = keywordSet(query);
            for (VectorStoreService.SearchResult r : results) {
                Set<String> cTerms = keywordSet(r.chunk.getText());
                double overlap = qTerms.isEmpty() ? 0 :
                        (double) qTerms.stream().filter(cTerms::contains).count() / qTerms.size();
                r.score += 0.1 * overlap;
            }
            results.sort((a, b) -> Double.compare(b.score, a.score));
        }

        // Re-rank: section match + length penalty
        if (rerank) {
            Set<String> qTerms = keywordSet(query);
            for (VectorStoreService.SearchResult r : results) {
                double score = r.score;
                if (r.chunk.getSection() != null) {
                    Set<String> sTerms = keywordSet(r.chunk.getSection());
                    score += 0.05 * qTerms.stream().filter(sTerms::contains).count();
                }
                if (r.chunk.getTokenCount() != null && r.chunk.getTokenCount() < 20) score -= 0.1;
                r.score = Math.max(0, score);
            }
            results.sort((a, b) -> Double.compare(b.score, a.score));
            for (int i = 0; i < results.size(); i++) results.get(i).rank = i + 1;
        }

        return results.subList(0, Math.min(topK, results.size()));
    }

    private Set<String> keywordSet(String text) {
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }
}
