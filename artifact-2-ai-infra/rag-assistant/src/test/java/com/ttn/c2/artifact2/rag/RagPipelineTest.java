package com.ttn.c2.artifact2.rag;

import com.ttn.c2.artifact2.rag.model.DocumentChunk;
import com.ttn.c2.artifact2.rag.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RagPipelineTest {

    private final EmbeddingService embedder = new EmbeddingService();
    private final ChunkingService chunker = new ChunkingService();
    private final VectorStoreService store = new VectorStoreService();

    @Test
    @DisplayName("Embedding is deterministic and normalized")
    void embeddingDeterministic() {
        List<Float> v1 = embedder.embed("hello world");
        List<Float> v2 = embedder.embed("hello world");
        assertThat(v1).isEqualTo(v2);
        assertThat(v1).hasSize(384);

        // L2 norm ≈ 1
        double norm = 0;
        for (float f : v1) norm += f * f;
        assertThat(Math.sqrt(norm)).isCloseTo(1.0, org.assertj.core.api.Assertions.within(1e-6));
    }

    @Test
    @DisplayName("Cosine similarity: identical = 1, different < 1")
    void cosineSimilarity() {
        List<Float> a = embedder.embed("auth token refresh flow");
        List<Float> b = embedder.embed("auth token refresh flow");
        List<Float> c = embedder.embed("payments rate limit configuration");
        assertThat(embedder.cosineSimilarity(a, b)).isCloseTo(1.0, within(1e-6));
        assertThat(embedder.cosineSimilarity(a, c)).isLessThan(0.9);
    }

    @Test
    @DisplayName("Chunking produces chunks with metadata")
    void chunking() {
        String text = "# Auth Service\n\nToken refresh rotates tokens.\n\n# Errors\n\n401 invalid token.";
        List<DocumentChunk> chunks = chunker.chunkRecursiveSemantic(text, "auth.md", 600);
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).getSourceFile()).isEqualTo("auth.md");
        assertThat(chunks.stream().allMatch(c -> c.getId() != null)).isTrue();
    }

    @Test
    @DisplayName("Vector store search returns ranked results")
    void vectorSearch() {
        DocumentChunk c1 = new DocumentChunk("d:0", "auth token refresh flow", "auth.md", null, null, 0, 5);
        DocumentChunk c2 = new DocumentChunk("d:1", "payments rate limit config", "payments.md", null, null, 1, 4);
        store.add(c1, embedder.embed("auth token refresh flow"));
        store.add(c2, embedder.embed("payments rate limit configuration"));

        List<VectorStoreService.SearchResult> results = store.search(embedder.embed("how does token refresh work"), 2);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).score).isGreaterThanOrEqualTo(results.get(1).score);
    }

    @Test
    @DisplayName("Retrieval with hybrid boost and re-rank works")
    void retrieval() {
        DocumentChunk c1 = new DocumentChunk("d:0", "auth token refresh flow uses rotation", "auth.md", "## Rotation", null, 0, 6);
        DocumentChunk c2 = new DocumentChunk("d:1", "unrelated topic about weather", "weather.md", null, null, 0, 4);
        VectorStoreService store2 = new VectorStoreService();
        store2.add(c1, embedder.embed("auth token refresh flow uses rotation"));
        store2.add(c2, embedder.embed("unrelated topic about weather"));

        RetrievalService retriever = new RetrievalService(embedder, store2);
        List<VectorStoreService.SearchResult> results = retriever.retrieve("auth token refresh", 2, 0.1, true, true);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).chunk.getSourceFile()).isEqualTo("auth.md");
    }

    @Test
    @DisplayName("Generation produces grounded answer with citations")
    void generation() {
        DocumentChunk c = new DocumentChunk("d:0", "auth uses refresh token rotation", "auth.md", null, null, 0, 5);
        VectorStoreService.SearchResult r = new VectorStoreService.SearchResult(c, 0.9, 1);
        GenerationService gen = new GenerationService();
        var result = gen.generate(new com.ttn.c2.artifact2.rag.model.RagQuery("how does auth refresh work"),
                List.of(r), 10, new com.ttn.c2.artifact2.rag.model.PipelineStep[0]);
        assertThat(result.isGrounded()).isTrue();
        assertThat(result.getCitations()).hasSize(1);
        assertThat(result.getCitations().get(0).getSourceFile()).isEqualTo("auth.md");
    }

    @Test
    @DisplayName("Eval metrics are within [0,1]")
    void evalMetrics() throws Exception {
        EvalService eval = new EvalService();
        var metrics = eval.runEval(
                java.nio.file.Paths.get("src/main/resources/static/docs"),
                java.nio.file.Paths.get("src/main/resources/eval/labeled_qa.jsonl"),
                5, 0.3);
        assertThat(metrics.getRetrievalHitRate()).isBetween(0.0, 1.0);
        assertThat(metrics.getAnswerAccuracy()).isBetween(0.0, 1.0);
        assertThat(metrics.getCitationValidity()).isBetween(0.0, 1.0);
        assertThat(metrics.getTotalQuestions()).isEqualTo(5);
    }
}
