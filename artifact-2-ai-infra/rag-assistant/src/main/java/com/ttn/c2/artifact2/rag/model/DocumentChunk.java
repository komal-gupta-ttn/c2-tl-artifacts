package com.ttn.c2.artifact2.rag.model;

import java.util.List;

/**
 * A document chunk with enriched metadata for filtered retrieval.
 *
 * Topic: Chunking & Retrieval Pipeline Design (TL rows 8-9)
 */
public class DocumentChunk {

    private String id;
    private String text;
    private String sourceFile;
    private String section;
    private Integer page;
    private Integer chunkIndex;
    private Integer tokenCount;
    private List<Float> embedding;

    public DocumentChunk() {}

    public DocumentChunk(String id, String text, String sourceFile,
                         String section, Integer page, Integer chunkIndex, Integer tokenCount) {
        this.id = id;
        this.text = text;
        this.sourceFile = sourceFile;
        this.section = section;
        this.page = page;
        this.chunkIndex = chunkIndex;
        this.tokenCount = tokenCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
}
