package com.fklein.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class OpenAIService {

    private static final Logger LOG = Logger.getLogger(OpenAIService.class);

    @ConfigProperty(name = "openai.api.key")
    String apiKey;

    @ConfigProperty(name = "openai.model", defaultValue = "gpt-4o-mini")
    String model;

    @ConfigProperty(name = "openai.max.tokens", defaultValue = "4096")
    int maxTokens;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a message to OpenAI and get a streaming response
     */
    public Multi<String> streamChatCompletion(String systemPrompt, String userMessage) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                URL url = new URL(OPENAI_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                // Build request body
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "max_tokens", maxTokens,
                        "stream", true,
                        "messages", List.of(
                                Map.of(
                                        "role", "system",
                                        "content", systemPrompt
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", userMessage
                                )
                        )
                );

                String jsonRequest = objectMapper.writeValueAsString(requestBody);
                LOG.info("Sending request to OpenAI API: " + model);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    LOG.error("OpenAI API error: " + error);
                    emitter.fail(new RuntimeException("OpenAI API error: " + responseCode + " - " + error));
                    return;
                }

                // Read streaming response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            try {
                                Map<String, Object> event = objectMapper.readValue(data, Map.class);
                                List<Map<String, Object>> choices = (List<Map<String, Object>>) event.get("choices");

                                if (choices != null && !choices.isEmpty()) {
                                    Map<String, Object> choice = choices.get(0);
                                    Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

                                    if (delta != null && delta.containsKey("content")) {
                                        String content = (String) delta.get("content");
                                        if (content != null && !content.isEmpty()) {
                                            emitter.emit(content);
                                        }
                                    }

                                    // Check for finish reason
                                    String finishReason = (String) choice.get("finish_reason");
                                    if ("stop".equals(finishReason)) {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Failed to parse event: " + data, e);
                            }
                        }
                    }
                }

                emitter.complete();

            } catch (Exception e) {
                LOG.error("Error calling OpenAI API", e);
                emitter.fail(e);
            }
        });
    }

    /**
     * Send a message to OpenAI and get a non-streaming response
     */
    public String chatCompletion(String systemPrompt, String userMessage) {
        try {
            URL url = new URL(OPENAI_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "stream", false,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", systemPrompt
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", userMessage
                            )
                    )
            );

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                LOG.error("OpenAI API error: " + error);
                throw new RuntimeException("OpenAI API error: " + responseCode);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }

            return "";

        } catch (Exception e) {
            LOG.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }
}
