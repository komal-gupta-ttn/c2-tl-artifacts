package com.ttn.c2.artifact2.rag.controller;

import com.ttn.c2.artifact2.rag.model.*;
import com.ttn.c2.artifact2.rag.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Controller
@RequestMapping("/artifact2")
public class RagController {

    private final EmbeddingService embedder = new EmbeddingService();
    private final ChunkingService chunker = new ChunkingService();
    private final GenerationService generator = new GenerationService();
    private final Path docsDir = Paths.get("src/main/resources/static/docs");

    @GetMapping
    public String index(Model model) {
        model.addAttribute("steps", pipelineSteps());
        return "artifact2/index";
    }

    @GetMapping("/pipeline")
    public String pipeline(Model model) {
        model.addAttribute("steps", pipelineSteps());
        return "artifact2/pipeline";
    }

    @GetMapping("/query")
    public String queryForm(Model model) {
        model.addAttribute("ragQuery", new RagQuery());
        return "artifact2/query";
    }

    @PostMapping("/ask")
    public String ask(@ModelAttribute RagQuery ragQuery, Model model) throws Exception {
        long start = System.nanoTime();

        // (Re)build index from docs each time for demo simplicity
        VectorStoreService store = new VectorStoreService();
        DocumentService docService = new DocumentService();
        List<DocumentChunk> allChunks = new ArrayList<>();
        if (Files.exists(docsDir)) {
            List<Map.Entry<String, String>> docs = docService.loadDocs(docsDir);
            for (Map.Entry<String, String> e : docs) {
                List<DocumentChunk> chunks = chunker.chunkRecursiveSemantic(e.getValue(), e.getKey(), 600);
                allChunks.addAll(chunks);
            }
        }
        List<List<Float>> vecs = embedder.embedBatch(allChunks.stream()
                .map(DocumentChunk::getText).toList());
        for (int i = 0; i < allChunks.size(); i++) store.add(allChunks.get(i), vecs.get(i));

        RetrievalService retriever = new RetrievalService(embedder, store);
        List<VectorStoreService.SearchResult> results =
                retriever.retrieve(ragQuery.getQuestion(), ragQuery.getTopK(),
                        ragQuery.getMinSimilarity(), true, true);

        PipelineStep[] steps = pipelineSteps();
        // Fill step outputs with live data
        steps[0].setOutput("Loaded " + allChunks.size() + " chunks from " +
                new HashSet<>(allChunks.stream().map(DocumentChunk::getSourceFile).toList()).size() + " docs");
        steps[1].setOutput("Recursive/semantic chunking with metadata enrichment (source, section, page)");
        steps[2].setOutput("Deterministic pseudo-embedding, dim=" + embedder.getDim() + ", L2-normalized");
        steps[3].setOutput("Indexed " + store.size() + " vectors in-memory (ChromaDB/PGVector swap)");
        steps[4].setOutput("Embedded query: \"" + ragQuery.getQuestion() + "\"");
        steps[5].setOutput("Retrieved " + results.size() + " chunks (top-K=" + ragQuery.getTopK() +
                ", min-sim=" + ragQuery.getMinSimilarity() + ")");
        steps[6].setOutput("Generated answer with " + results.size() + " citations");

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        var result = generator.generate(ragQuery, results, durationMs, steps);
        model.addAttribute("result", result);
        model.addAttribute("ragQuery", ragQuery);
        return "artifact2/result";
    }

    @GetMapping("/eval")
    public String eval(Model model) throws Exception {
        EvalService eval = new EvalService();
        Path qaPath = docsDir.resolveSibling("eval/labeled_qa.jsonl");
        EvalMetrics metrics = eval.runEval(docsDir, qaPath, 5, 0.3);
        model.addAttribute("metrics", metrics);
        return "artifact2/eval";
    }

    private PipelineStep[] pipelineSteps() {
        return new PipelineStep[]{
            new PipelineStep(1, "Source Documents", "Load project docs/codebase to index",
                    "docs/sample_docs/", "—", null, 0, "OK"),
            new PipelineStep(2, "Chunking", "Recursive/semantic chunking + metadata enrichment",
                    "raw docs", "chunks (id, text, source, section, page)", null, 0, "OK"),
            new PipelineStep(3, "Embedding", "Dense vector generation (nomic-embed-text / Titan / fallback)",
                    "chunk text", "384-dim L2-normalized vector", null, 0, "OK"),
            new PipelineStep(4, "Vector Store", "Index vectors for similarity search",
                    "embeddings", "ChromaDB / PGVector (in-memory for demo)", null, 0, "OK"),
            new PipelineStep(5, "Query Processing", "Embed user query into same vector space",
                    "natural language query", "query vector", null, 0, "OK"),
            new PipelineStep(6, "Retrieval", "Vector search + hybrid keyword boost + re-rank",
                    "query vector", "top-K chunks with scores", null, 0, "OK"),
            new PipelineStep(7, "Generation", "Assemble context → generate answer with citations",
                    "retrieved chunks + query", "answer + source citations", null, 0, "OK"),
            new PipelineStep(8, "Evaluation", "Precision/recall, answer-accuracy, citation validity",
                    "labeled Q&A set", "eval metrics scorecard", null, 0, "OK")
        };
    }
}
