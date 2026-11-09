package com.ttn.c2.artifact2.rag.model;

import java.util.List;

/**
 * End-to-end RAG result: answer + citations + pipeline metrics for the step-by-step UI.
 */
public class RagResult {

    private String question;
    private String answer;
    private List<Citation> citations;
    private boolean grounded;
    private int numResults;
    private long durationMs;
    private PipelineStep[] steps;

    public RagResult() {}

    public RagResult(String question, String answer, List<Citation> citations,
                     boolean grounded, int numResults, long durationMs, PipelineStep[] steps) {
        this.question = question;
        this.answer = answer;
        this.citations = citations;
        this.grounded = grounded;
        this.numResults = numResults;
        this.durationMs = durationMs;
        this.steps = steps;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<Citation> getCitations() { return citations; }
    public void setCitations(List<Citation> citations) { this.citations = citations; }
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
    public int getNumResults() { return numResults; }
    public void setNumResults(int numResults) { this.numResults = numResults; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public PipelineStep[] getSteps() { return steps; }
    public void setSteps(PipelineStep[] steps) { this.steps = steps; }
}
