package com.ttn.c2.artifact2.rag.model;

/**
 * Lightweight RAG evaluation metrics (TL row 10: RAG Architecture & Evaluation).
 */
public class EvalMetrics {

    private int totalQuestions;
    private double retrievalHitRate;
    private double answerAccuracy;
    private double citationValidity;
    private double avgPrecisionAtK;
    private double avgRecallAtK;

    public EvalMetrics() {}

    public EvalMetrics(int totalQuestions, double retrievalHitRate, double answerAccuracy,
                       double citationValidity, double avgPrecisionAtK, double avgRecallAtK) {
        this.totalQuestions = totalQuestions;
        this.retrievalHitRate = retrievalHitRate;
        this.answerAccuracy = answerAccuracy;
        this.citationValidity = citationValidity;
        this.avgPrecisionAtK = avgPrecisionAtK;
        this.avgRecallAtK = avgRecallAtK;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public double getRetrievalHitRate() { return retrievalHitRate; }
    public void setRetrievalHitRate(double retrievalHitRate) { this.retrievalHitRate = retrievalHitRate; }
    public double getAnswerAccuracy() { return answerAccuracy; }
    public void setAnswerAccuracy(double answerAccuracy) { this.answerAccuracy = answerAccuracy; }
    public double getCitationValidity() { return citationValidity; }
    public void setCitationValidity(double citationValidity) { this.citationValidity = citationValidity; }
    public double getAvgPrecisionAtK() { return avgPrecisionAtK; }
    public void setAvgPrecisionAtK(double avgPrecisionAtK) { this.avgPrecisionAtK = avgPrecisionAtK; }
    public double getAvgRecallAtK() { return avgRecallAtK; }
    public void setAvgRecallAtK(double avgRecallAtK) { this.avgRecallAtK = avgRecallAtK; }
}
