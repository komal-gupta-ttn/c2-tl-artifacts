package com.ttn.c2.artifact1.model;

/**
 * Input payload for the review-summary generation demo.
 * Holds the raw unified diff and optional PR identifier.
 */
public class DiffInput {

    private String prId = "PR-DEMO";
    private String diff;

    public DiffInput() {}

    public DiffInput(String prId, String diff) {
        this.prId = prId;
        this.diff = diff;
    }

    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }
    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }
}
