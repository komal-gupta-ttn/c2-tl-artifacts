package com.ttn.c2.artifact2.rag.model;

/**
 * A source citation attached to a generated answer, validated against retrieved chunks.
 */
public class Citation {

    private String sourceFile;
    private String chunkId;
    private String section;
    private double score;
    private String snippet;

    public Citation() {}

    public Citation(String sourceFile, String chunkId, String section, double score, String snippet) {
        this.sourceFile = sourceFile;
        this.chunkId = chunkId;
        this.section = section;
        this.score = score;
        this.snippet = snippet;
    }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
}
