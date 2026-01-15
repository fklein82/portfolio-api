package com.fklein.services;

import com.fklein.models.DocumentChunk;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class VectorStoreService {

    private static final Logger LOG = Logger.getLogger(VectorStoreService.class);

    @Inject
    EmbeddingService embeddingService;

    // In-memory storage for document chunks
    private final Map<String, DocumentChunk> vectorStore = new ConcurrentHashMap<>();

    /**
     * Add a document chunk to the vector store
     */
    public void addChunk(DocumentChunk chunk) {
        if (chunk.getEmbedding() == null || chunk.getEmbedding().length == 0) {
            throw new IllegalArgumentException("Chunk must have an embedding");
        }
        vectorStore.put(chunk.getId(), chunk);
        LOG.info("Added chunk to vector store: " + chunk.getId());
    }

    /**
     * Add multiple document chunks to the vector store
     */
    public void addChunks(List<DocumentChunk> chunks) {
        chunks.forEach(this::addChunk);
        LOG.info("Added " + chunks.size() + " chunks to vector store");
    }

    /**
     * Search for similar chunks based on query embedding
     */
    public List<DocumentChunk> search(float[] queryEmbedding, int topK) {
        if (vectorStore.isEmpty()) {
            LOG.warn("Vector store is empty");
            return Collections.emptyList();
        }

        // Calculate similarity scores for all chunks
        List<DocumentChunk> results = vectorStore.values().stream()
                .map(chunk -> {
                    double score = embeddingService.cosineSimilarity(queryEmbedding, chunk.getEmbedding());
                    DocumentChunk result = new DocumentChunk();
                    result.setId(chunk.getId());
                    result.setContent(chunk.getContent());
                    result.setMetadata(chunk.getMetadata());
                    result.setEmbedding(chunk.getEmbedding());
                    result.setScore(score);
                    return result;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore())) // Sort by score descending
                .limit(topK)
                .collect(Collectors.toList());

        LOG.info("Search returned " + results.size() + " results");
        return results;
    }

    /**
     * Search for similar chunks based on query text
     */
    public List<DocumentChunk> searchByText(String queryText, int topK) {
        LOG.info("Searching for: " + queryText);
        float[] queryEmbedding = embeddingService.generateEmbedding(queryText);
        return search(queryEmbedding, topK);
    }

    /**
     * Get a chunk by ID
     */
    public Optional<DocumentChunk> getChunk(String id) {
        return Optional.ofNullable(vectorStore.get(id));
    }

    /**
     * Get all chunks
     */
    public List<DocumentChunk> getAllChunks() {
        return new ArrayList<>(vectorStore.values());
    }

    /**
     * Get the number of chunks in the store
     */
    public int size() {
        return vectorStore.size();
    }

    /**
     * Clear the vector store
     */
    public void clear() {
        vectorStore.clear();
        LOG.info("Vector store cleared");
    }

    /**
     * Remove a chunk by ID
     */
    public boolean removeChunk(String id) {
        boolean removed = vectorStore.remove(id) != null;
        if (removed) {
            LOG.info("Removed chunk: " + id);
        }
        return removed;
    }

    /**
     * Check if the store contains a chunk with the given ID
     */
    public boolean containsChunk(String id) {
        return vectorStore.containsKey(id);
    }
}
