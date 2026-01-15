package com.fklein.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
public class ClaudeService {

    private static final Logger LOG = Logger.getLogger(ClaudeService.class);

    @ConfigProperty(name = "claude.api.key")
    String apiKey;

    @ConfigProperty(name = "claude.model", defaultValue = "claude-sonnet-4-5-20250929")
    String model;

    @ConfigProperty(name = "claude.max.tokens", defaultValue = "4096")
    int maxTokens;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a message to Claude and get a streaming response
     */
    public Multi<String> streamChatCompletion(String systemPrompt, String userMessage) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                URL url = new URL(CLAUDE_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", apiKey);
                conn.setRequestProperty("anthropic-version", ANTHROPIC_VERSION);
                conn.setDoOutput(true);

                // Build request body
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "max_tokens", maxTokens,
                        "stream", true,
                        "system", systemPrompt,
                        "messages", List.of(
                                Map.of(
                                        "role", "user",
                                        "content", userMessage
                                )
                        )
                );

                String jsonRequest = objectMapper.writeValueAsString(requestBody);
                LOG.info("Sending request to Claude API: " + model);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    LOG.error("Claude API error: " + error);
                    emitter.fail(new RuntimeException("Claude API error: " + responseCode + " - " + error));
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
                                String type = (String) event.get("type");

                                if ("content_block_delta".equals(type)) {
                                    Map<String, Object> delta = (Map<String, Object>) event.get("delta");
                                    if (delta != null && "text_delta".equals(delta.get("type"))) {
                                        String text = (String) delta.get("text");
                                        if (text != null && !text.isEmpty()) {
                                            emitter.emit(text);
                                        }
                                    }
                                } else if ("message_stop".equals(type)) {
                                    break;
                                } else if ("error".equals(type)) {
                                    Map<String, Object> error = (Map<String, Object>) event.get("error");
                                    String errorMessage = (String) error.get("message");
                                    emitter.fail(new RuntimeException("Claude API error: " + errorMessage));
                                    return;
                                }
                            } catch (Exception e) {
                                LOG.warn("Failed to parse event: " + data, e);
                            }
                        }
                    }
                }

                emitter.complete();

            } catch (Exception e) {
                LOG.error("Error calling Claude API", e);
                emitter.fail(e);
            }
        });
    }

    /**
     * Send a message to Claude and get a non-streaming response
     */
    public String chatCompletion(String systemPrompt, String userMessage) {
        try {
            URL url = new URL(CLAUDE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", ANTHROPIC_VERSION);
            conn.setDoOutput(true);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "stream", false,
                    "system", systemPrompt,
                    "messages", List.of(
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
                LOG.error("Claude API error: " + error);
                throw new RuntimeException("Claude API error: " + responseCode);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");
            if (content != null && !content.isEmpty()) {
                return (String) content.get(0).get("text");
            }

            return "";

        } catch (Exception e) {
            LOG.error("Error calling Claude API", e);
            throw new RuntimeException("Failed to call Claude API", e);
        }
    }
}
