package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Evaluation harness — Artifact 2 RAG assistant.
 *
 * Topic: RAG Architecture & Evaluation (TL row 10)
 *
 * Lightweight eval per C2 TL requirements:
 *   - Labeled Q&A set (question → expected answer → source doc)
 *   - Retrieval precision/recall
 *   - Answer-accuracy scoring
 *   - Citation correctness check
 */
@Service
public class EvalService {

    public static class QaItem {
        public String question;
        public String expectedSource;
        public List<String> expectedKeywords = new ArrayList<>();
    }

    public EvalMetrics runEval(Path docsDir, Path qaPath, int topK, double minSim) throws IOException {
        DocumentService docService = new DocumentService();
        List<Map.Entry<String, String>> docs = docService.loadDocs(docsDir);

        EmbeddingService embedder = new EmbeddingService();
        ChunkingService chunker = new ChunkingService();
        VectorStoreService store = new VectorStoreService();

        List<DocumentChunk> allChunks = new ArrayList<>();
        for (Map.Entry<String, String> e : docs) {
            List<DocumentChunk> chunks = chunker.chunkRecursiveSemantic(e.getValue(), e.getKey(), 600);
            allChunks.addAll(chunks);
        }
        List<List<Float>> vecs = embedder.embedBatch(allChunks.stream()
                .map(DocumentChunk::getText).toList());
        for (int i = 0; i < allChunks.size(); i++) store.add(allChunks.get(i), vecs.get(i));

        RetrievalService retriever = new RetrievalService(embedder, store);
        GenerationService generator = new GenerationService();

        List<QaItem> qa = loadQa(qaPath);
        int total = qa.size();
        int retrievalHits = 0, answerCorrect = 0, citationValid = 0;
        double precisionSum = 0, recallSum = 0;
        int pCount = 0, rCount = 0;

        for (QaItem item : qa) {
            List<VectorStoreService.SearchResult> results =
                    retriever.retrieve(item.question, topK, minSim, true, true);

            List<String> retrievedSources = results.stream()
                    .map(r -> r.chunk.getSourceFile()).toList();
            if (item.expectedSource != null && retrievedSources.contains(item.expectedSource)) retrievalHits++;

            if (!results.isEmpty()) {
                long relevant = results.stream()
                        .filter(r -> item.expectedKeywords.stream()
                                .anyMatch(kw -> r.chunk.getText().toLowerCase().contains(kw.toLowerCase())))
                        .count();
                precisionSum += (double) relevant / results.size();
                pCount++;
            }
            if (item.expectedSource != null) {
                boolean found = retrievedSources.contains(item.expectedSource);
                recallSum += found ? 1.0 : 0.0;
                rCount++;
            }

            var answer = generator.generate(new RagQuery(item.question), results, 0, new PipelineStep[0]);
            String ansText = answer.getAnswer().toLowerCase();
            long kwHits = item.expectedKeywords.stream()
                    .filter(kw -> ansText.contains(kw.toLowerCase())).count();
            if (!item.expectedKeywords.isEmpty() && kwHits / (double) item.expectedKeywords.size() >= 0.5)
                answerCorrect++;

            List<String> citedIds = answer.getCitations().stream()
                    .map(Citation::getChunkId).toList();
            if (!citedIds.isEmpty()) {
                Set<String> validIds = results.stream()
                        .map(r -> r.chunk.getId()).collect(Collectors.toSet());
                if (citedIds.stream().allMatch(validIds::contains)) citationValid++;
            }
        }

        return new EvalMetrics(
                total,
                round(retrievalHits / (double) total, 3),
                round(answerCorrect / (double) total, 3),
                round(citationValid / (double) total, 3),
                round(precisionSum / Math.max(pCount, 1), 3),
                round(recallSum / Math.max(rCount, 1), 3));
    }

    private List<QaItem> loadQa(Path qaPath) throws IOException {
        List<QaItem> items = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(qaPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                QaItem item = new QaItem();
                // Minimal JSON parse (avoid external dep)
                item.question = extractJsonField(line, "question");
                item.expectedSource = extractJsonField(line, "expected_source");
                String kws = extractJsonField(line, "expected_keywords");
                if (kws != null && kws.startsWith("[")) {
                    String inner = kws.substring(1, kws.length() - 1);
                    for (String kw : inner.split(",")) {
                        kw = kw.trim().replaceAll("^\"|\"$", "");
                        if (!kw.isEmpty()) item.expectedKeywords.add(kw);
                    }
                }
                items.add(item);
            }
        }
        return items;
    }

    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? null : json.substring(start, end);
    }

    private double round(double v, int places) {
        double scale = Math.pow(10, places);
        return Math.round(v * scale) / scale;
    }
}
