package com.ttn.c2.artifact1.model;

/**
 * Traceability matrix row: spec ref → acceptance criterion → task → commit → PR → tests.
 */
public class TraceabilityEntry {

    private String specRef;
    private String acceptanceCriterion;
    private String taskId;
    private String commit;
    private String pr;
    private String tests;
    private String status;

    public TraceabilityEntry() {}

    public TraceabilityEntry(String specRef, String acceptanceCriterion, String taskId,
                             String commit, String pr, String tests, String status) {
        this.specRef = specRef;
        this.acceptanceCriterion = acceptanceCriterion;
        this.taskId = taskId;
        this.commit = commit;
        this.pr = pr;
        this.tests = tests;
        this.status = status;
    }

    public String getSpecRef() { return specRef; }
    public void setSpecRef(String specRef) { this.specRef = specRef; }
    public String getAcceptanceCriterion() { return acceptanceCriterion; }
    public void setAcceptanceCriterion(String acceptanceCriterion) { this.acceptanceCriterion = acceptanceCriterion; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getCommit() { return commit; }
    public void setCommit(String commit) { this.commit = commit; }
    public String getPr() { return pr; }
    public void setPr(String pr) { this.pr = pr; }
    public String getTests() { return tests; }
    public void setTests(String tests) { this.tests = tests; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
