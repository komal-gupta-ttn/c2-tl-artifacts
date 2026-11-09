package com.ttn.c2.artifact1.service;

/**
 * Placeholder for the prompt-engineering service.
 * In production this wires into an LLM (Claude via Bedrock / Ollama) with
 * structured-output JSON schema. For the demo, logic lives in ReviewSummaryService.
 *
 * Techniques demonstrated:
 *   - Structured output (JSON schema)
 *   - Few-shot examples
 *   - Prompt chaining (validate → generate → ground → render)
 *   - Self-critique prompting
 */
public class PromptService {
    // Kept minimal for demo; generation logic is in ReviewSummaryService
}
