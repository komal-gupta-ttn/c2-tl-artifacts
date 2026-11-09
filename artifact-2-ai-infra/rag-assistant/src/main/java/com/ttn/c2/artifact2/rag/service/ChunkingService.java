package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chunking service — Artifact 2 RAG assistant.
 *
 * Topic: Chunking & Retrieval Pipeline Design (TL rows 8-9)
 *
 * Strategies: fixed-size, paragraph-based, recursive/semantic.
 * Metadata enrichment: source file, section, page.
 */
@Service
public class ChunkingService {

    private static final Pattern HEADER_RE = Pattern.compile("(?m)^(#{1,6}\\s+.+)$");

    public List<DocumentChunk> chunkRecursiveSemantic(String text, String sourceFile, int maxChunkSize) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String[] parts = HEADER_RE.split(text, -1);
        Matcher headerMatcher = HEADER_RE.matcher(text);
        int idx = 0;
        String currentSection = null;

        // Re-scan to pair headers with content
        java.util.List<String> segments = new ArrayList<>();
        java.util.List<String> sectionFor = new ArrayList<>();
        int lastEnd = 0;
        while (headerMatcher.find()) {
            String before = text.substring(lastEnd, headerMatcher.start()).trim();
            if (!before.isEmpty()) {
                segments.add(before);
                sectionFor.add(currentSection);
            }
            currentSection = headerMatcher.group().trim();
            lastEnd = headerMatcher.end();
        }
        String after = text.substring(lastEnd).trim();
        if (!after.isEmpty()) {
            segments.add(after);
            sectionFor.add(currentSection);
        }
        if (segments.isEmpty() && !text.trim().isEmpty()) {
            segments.add(text.trim());
            sectionFor.add(null);
        }

        for (int i = 0; i < segments.size(); i++) {
            String seg = segments.get(i);
            String section = sectionFor.get(i);
            if (seg.split("\\s+").length <= maxChunkSize) {
                chunks.add(new DocumentChunk(
                        sourceFile + ":" + idx, seg, sourceFile, section, null, idx++, seg.split("\\s+").length));
            } else {
                // Fall back to fixed-size within oversized segment
                List<DocumentChunk> sub = chunkFixedSize(seg, sourceFile, maxChunkSize, 50, section);
                for (DocumentChunk c : sub) {
                    c.setId(sourceFile + ":" + idx);
                    c.setChunkIndex(idx++);
                    chunks.add(c);
                }
            }
        }
        return chunks;
    }

    public List<DocumentChunk> chunkFixedSize(String text, String sourceFile,
                                              int chunkSize, int overlap, String section) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        int i = 0, idx = 0;
        while (i < words.length) {
            int end = Math.min(i + chunkSize, words.length);
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) sb.append(words[j]).append(" ");
            chunks.add(new DocumentChunk(
                    sourceFile + ":" + (idx), sb.toString().trim(), sourceFile, section, null, idx++, end - i));
            i += chunkSize - overlap;
        }
        return chunks;
    }

    public List<DocumentChunk> chunkParagraphs(String text, String sourceFile, int maxChunkSize) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String[] paras = text.split("\\n\\n");
        int idx = 0;
        for (String p : paras) {
            p = p.trim();
            if (p.isEmpty()) continue;
            if (p.split("\\s+").length <= maxChunkSize) {
                chunks.add(new DocumentChunk(
                        sourceFile + ":" + idx, p, sourceFile, inferSection(p), null, idx++, p.split("\\s+").length));
            } else {
                chunks.addAll(chunkFixedSize(p, sourceFile, maxChunkSize, 50, inferSection(p)));
                idx += chunks.size(); // approximate
            }
        }
        return chunks;
    }

    private String inferSection(String text) {
        String firstLine = text.strip().split("\\n")[0];
        if (firstLine.startsWith("#")) return firstLine.replaceAll("^#{1,6}\\s+", "").trim();
        return null;
    }
}
