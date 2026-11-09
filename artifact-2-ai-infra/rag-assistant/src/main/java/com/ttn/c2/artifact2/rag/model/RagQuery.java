package com.ttn.c2.artifact2.rag.model;

public class RagQuery {

    private String question;
    private int topK = 5;
    private double minSimilarity = 0.3;

    public RagQuery() {}

    public RagQuery(String question) {
        this.question = question;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public double getMinSimilarity() { return minSimilarity; }
    public void setMinSimilarity(double minSimilarity) { this.minSimilarity = minSimilarity; }
}
