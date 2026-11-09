package com.ttn.c2.artifact1.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete code review summary produced by the AI generator.
 *
 * AC coverage:
 *   AC-01: Review summary generated from PR diff (structured output)
 *   AC-02: Invalid/malformed diff returns error
 *   AC-03: Review completes within 2s for diffs < 500 lines
 *   AC-04: Findings grounded in actual diff lines (no hallucination)
 */
public class ReviewSummary {

    private String prId;
    private int filesChanged;
    private int linesAdded;
    private int linesRemoved;
    private List<ReviewFinding> findings = new ArrayList<>();
    private String summaryMarkdown;
    private Instant generatedAt;
    private long durationMs;
    private int groundedCount;
    private int totalFindings;

    public ReviewSummary() {}

    public ReviewSummary(String prId, int filesChanged, int linesAdded, int linesRemoved,
                         List<ReviewFinding> findings, String summaryMarkdown,
                         Instant generatedAt, long durationMs, int groundedCount, int totalFindings) {
        this.prId = prId;
        this.filesChanged = filesChanged;
        this.linesAdded = linesAdded;
        this.linesRemoved = linesRemoved;
        this.findings = findings;
        this.summaryMarkdown = summaryMarkdown;
        this.generatedAt = generatedAt;
        this.durationMs = durationMs;
        this.groundedCount = groundedCount;
        this.totalFindings = totalFindings;
    }

    // Getters & setters
    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }
    public int getFilesChanged() { return filesChanged; }
    public void setFilesChanged(int filesChanged) { this.filesChanged = filesChanged; }
    public int getLinesAdded() { return linesAdded; }
    public void setLinesAdded(int linesAdded) { this.linesAdded = linesAdded; }
    public int getLinesRemoved() { return linesRemoved; }
    public void setLinesRemoved(int linesRemoved) { this.linesRemoved = linesRemoved; }
    public List<ReviewFinding> getFindings() { return findings; }
    public void setFindings(List<ReviewFinding> findings) { this.findings = findings; }
    public String getSummaryMarkdown() { return summaryMarkdown; }
    public void setSummaryMarkdown(String summaryMarkdown) { this.summaryMarkdown = summaryMarkdown; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public int getGroundedCount() { return groundedCount; }
    public void setGroundedCount(int groundedCount) { this.groundedCount = groundedCount; }
    public int getTotalFindings() { return totalFindings; }
    public void setTotalFindings(int totalFindings) { this.totalFindings = totalFindings; }
}
