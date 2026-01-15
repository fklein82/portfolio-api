package com.fklein.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessage {

    @JsonProperty("message")
    private String message;

    @JsonProperty("role")
    private String role; // "user" or "assistant"

    @JsonProperty("timestamp")
    private Long timestamp;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String message, String role) {
        this.message = message;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message='" + message + '\'' +
                ", role='" + role + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
