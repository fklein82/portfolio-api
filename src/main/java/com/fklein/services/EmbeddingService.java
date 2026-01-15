package com.fklein.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class EmbeddingService {

    private static final Logger LOG = Logger.getLogger(EmbeddingService.class);

    @ConfigProperty(name = "embeddings.provider", defaultValue = "voyageai")
    String provider;

    @ConfigProperty(name = "embeddings.api.key")
    String apiKey;

    @ConfigProperty(name = "embeddings.model", defaultValue = "voyage-3")
    String model;

    private static final String VOYAGE_API_URL = "https://api.voyageai.com/v1/embeddings";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate embeddings for a single text
     */
    public float[] generateEmbedding(String text) {
        try {
            String apiUrl = getApiUrl();
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            if ("voyageai".equals(provider)) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            } else if ("openai".equals(provider)) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            }

            conn.setDoOutput(true);

            Map<String, Object> requestBody = Map.of(
                    "input", text,
                    "model", model
            );

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                LOG.error("Embedding API error: " + error);
                throw new RuntimeException("Embedding API error: " + responseCode);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data != null && !data.isEmpty()) {
                List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");

                // Convert List<Double> to float[]
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }

                LOG.info("Generated embedding of size: " + embedding.length);
                return embedding;
            }

            throw new RuntimeException("No embedding returned from API");

        } catch (Exception e) {
            LOG.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Generate embeddings for multiple texts (batch)
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        try {
            String apiUrl = getApiUrl();
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            if ("voyageai".equals(provider)) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            } else if ("openai".equals(provider)) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            }

            conn.setDoOutput(true);

            Map<String, Object> requestBody = Map.of(
                    "input", texts,
                    "model", model
            );

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                LOG.error("Embedding API error: " + error);
                throw new RuntimeException("Embedding API error: " + responseCode);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data != null && !data.isEmpty()) {
                return data.stream().map(item -> {
                    List<Double> embeddingList = (List<Double>) item.get("embedding");
                    float[] embedding = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embedding[i] = embeddingList.get(i).floatValue();
                    }
                    return embedding;
                }).toList();
            }

            throw new RuntimeException("No embeddings returned from API");

        } catch (Exception e) {
            LOG.error("Error generating embeddings", e);
            throw new RuntimeException("Failed to generate embeddings", e);
        }
    }

    private String getApiUrl() {
        return switch (provider) {
            case "voyageai" -> VOYAGE_API_URL;
            case "openai" -> OPENAI_API_URL;
            default -> throw new IllegalArgumentException("Unsupported embeddings provider: " + provider);
        };
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Embeddings must have the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
