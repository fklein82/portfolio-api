package com.fklein.models;

import java.util.Map;

public class DocumentChunk {

    private String id;
    private String content;
    private float[] embedding;
    private Map<String, String> metadata;
    private double score; // For similarity search results

    public DocumentChunk() {
    }

    public DocumentChunk(String id, String content, Map<String, String> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DocumentChunk{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", score=" + score +
                ", metadata=" + metadata +
                '}';
    }
}
