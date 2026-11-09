package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generation service — Artifact 2 RAG assistant.
 *
 * Topic: RAG Architecture & Evaluation (TL row 10)
 *
 * Assembles retrieved context into a prompt, generates an answer with citations,
 * and validates citations against retrieved docs (catches hallucinated citations).
 *
 * In production this calls an LLM (Claude via Bedrock / Ollama) with a RAG system
 * prompt. For the demo, a deterministic template runs without an LLM key.
 */
@Service
public class GenerationService {

    public RagResult generate(RagQuery query, List<VectorStoreService.SearchResult> results,
                              long durationMs, PipelineStep[] steps) {
        if (results.isEmpty()) {
            return new RagResult(
                    query.getQuestion(),
                    "I couldn't find any relevant documentation for that question. " +
                            "Try rephrasing or check that the docs have been indexed.",
                    List.of(), true, 0, durationMs, steps);
        }

        List<Citation> citations = new ArrayList<>();
        StringBuilder contextParts = new StringBuilder();
        for (VectorStoreService.SearchResult r : results) {
            DocumentChunk c = r.chunk;
            String ctx = "[" + c.getSourceFile() + "#" + c.getChunkIndex() + "] " + c.getText();
            contextParts.append(ctx).append("\n\n---\n\n");
            citations.add(new Citation(
                    c.getSourceFile(), c.getId(), c.getSection(), r.score,
                    c.getText().length() > 200 ? c.getText().substring(0, 200) + "…" : c.getText()));
        }

        String answer = renderAnswer(query.getQuestion(), contextParts.toString(), citations);
        boolean grounded = citations.stream().allMatch(c -> c.getSourceFile() != null);

        return new RagResult(query.getQuestion(), answer, citations, grounded,
                results.size(), durationMs, steps);
    }

    private String renderAnswer(String question, String context, List<Citation> citations) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Answer\n\n");
        sb.append("Based on the indexed documentation, here's what I found for your question.\n\n");
        sb.append("### Supporting evidence\n");
        for (Citation c : citations) {
            sb.append("- **").append(c.getSourceFile()).append("** (score ")
                    .append(String.format("%.4f", c.getScore())).append("): ")
                    .append(c.getSnippet()).append("…\n");
        }
        sb.append("\n### Sources\n");
        for (Citation c : citations) {
            String sec = c.getSection() != null ? " · section: " + c.getSection() : "";
            sb.append("- ").append(c.getSourceFile()).append("#")
                    .append(c.getChunkId().substring(c.getChunkId().lastIndexOf(':') + 1))
                    .append(sec).append("\n");
        }
        sb.append("\n_Note: Template-generated answer. In production, replace with an LLM call using the retrieved context._");
        return sb.toString();
    }

    public boolean validateCitations(List<Citation> cited, List<VectorStoreService.SearchResult> results) {
        Set<String> validIds = results.stream()
                .map(r -> r.chunk.getId()).collect(Collectors.toSet());
        return cited.stream().allMatch(c -> validIds.contains(c.getChunkId()));
    }
}
