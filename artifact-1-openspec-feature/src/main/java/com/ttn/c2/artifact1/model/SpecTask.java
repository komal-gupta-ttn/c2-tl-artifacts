package com.ttn.c2.artifact1.model;

/**
 * Atomic, independently-verifiable task from the OpenSpec task breakdown.
 * Mirrors spec/feature-spec.md section 4 (T-01…T-06).
 */
public class SpecTask {

    private String id;
    private String description;
    private String acceptance;
    private String status; // TODO | IN_PROGRESS | DONE

    public SpecTask() {}

    public SpecTask(String id, String description, String acceptance, String status) {
        this.id = id;
        this.description = description;
        this.acceptance = acceptance;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAcceptance() { return acceptance; }
    public void setAcceptance(String acceptance) { this.acceptance = acceptance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
