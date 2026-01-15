package com.fklein.resources;

import com.fklein.models.ChatMessage;
import com.fklein.services.RagService;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatbotResource {

    private static final Logger LOG = Logger.getLogger(ChatbotResource.class);

    @Inject
    RagService ragService;

    /**
     * Stream chat response using Server-Sent Events
     */
    @POST
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> streamChat(ChatMessage message) {
        LOG.info("Received chat message: " + message.getMessage());

        if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            return Multi.createFrom().item("Erreur: Message vide");
        }

        return ragService.processQuery(message.getMessage());
    }

    /**
     * Non-streaming chat endpoint (for testing)
     */
    @POST
    public ChatMessage chat(ChatMessage message) {
        LOG.info("Received non-streaming chat message: " + message.getMessage());

        if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            return new ChatMessage("Erreur: Message vide", "assistant");
        }

        StringBuilder response = new StringBuilder();
        ragService.processQuery(message.getMessage())
                .subscribe().with(
                        chunk -> response.append(chunk),
                        failure -> LOG.error("Error in chat", failure)
                );

        // Wait a bit for the response to complete (not ideal, but works for testing)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new ChatMessage(response.toString(), "assistant");
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    public String health() {
        return "Chatbot is ready!";
    }
}
