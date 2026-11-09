package com.ttn.c2.artifact2.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Artifact 2 — Shared AI Infrastructure: RAG-Backed Assistant (TL differentiator).
 *
 * Builds and runs a RAG-backed assistant over project docs/codebase:
 *   docs → chunk → embed → store → query → retrieve → re-rank → generate → citations → eval
 *
 * TL expectations:
 *   1. Build and run shared AI infrastructure for the project team
 *   2. Be the go-to person when the project's AI tooling breaks or needs extending
 *
 * TL course block (~20 hrs):
 *   Embeddings & Vector Search · Chunking & Retrieval Pipeline Design · RAG Architecture & Evaluation
 */
@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
