package com.ttn.c2.artifact1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Artifact 1 — OpenSpec-Driven Feature Delivery (SE/SSE base).
 *
 * AI-assisted code review summary generator built entirely through a spec-first
 * OpenSpec/Speckit workflow: spec → task breakdown → AI-generated implementation
 * → self-review → PR with traceability.
 *
 * C2 SE/SSE expectations:
 *   1. Spec-first workflow (OpenSpec/Speckit) integrated with project Rules/Skills/MCP
 *   2. Self-review and iterate AI-generated code/tests against spec acceptance criteria
 *   3. Prompt-engineering techniques (chaining, caching-aware, structured output)
 */
@SpringBootApplication
public class Artifact1Application {

    public static void main(String[] args) {
        SpringApplication.run(Artifact1Application.class, args);
    }
}
