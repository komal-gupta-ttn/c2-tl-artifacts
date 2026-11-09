package com.ttn.c2.artifact2.rag.model;

import java.util.Map;

/**
 * A single step in the RAG pipeline, used by the step-by-step transformation UI.
 */
public class PipelineStep {

    private int order;
    private String name;
    private String description;
    private String input;
    private String output;
    private Map<String, Object> metadata;
    private long durationMs;
    private String status; // OK | WARN | ERROR

    public PipelineStep() {}

    public PipelineStep(int order, String name, String description,
                        String input, String output, Map<String, Object> metadata,
                        long durationMs, String status) {
        this.order = order;
        this.name = name;
        this.description = description;
        this.input = input;
        this.output = output;
        this.metadata = metadata;
        this.durationMs = durationMs;
        this.status = status;
    }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
