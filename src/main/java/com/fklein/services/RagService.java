package com.fklein.services;

import com.fklein.models.DocumentChunk;
import com.fklein.models.ProfileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RagService {

    private static final Logger LOG = Logger.getLogger(RagService.class);

    @Inject
    ClaudeService claudeService;

    @Inject
    EmbeddingService embeddingService;

    @Inject
    VectorStoreService vectorStoreService;

    private ProfileData profileData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize the RAG system on application startup
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Initializing RAG system...");

        // Always load profile data first (even if embeddings fail)
        try {
            loadProfileData();
        } catch (Exception e) {
            LOG.error("Failed to load profile data", e);
            throw new RuntimeException("Cannot start without profile data", e);
        }

        // Try to initialize embeddings and vector store
        try {
            indexCV();
            LOG.info("RAG system initialized successfully with " + vectorStoreService.size() + " chunks");
        } catch (Exception e) {
            LOG.warn("RAG indexing failed (chatbot will not work, but website will display). Error: " + e.getMessage());
        }
    }

    /**
     * Load profile data from JSON
     */
    private void loadProfileData() throws Exception {
        InputStream cvStream = getClass().getResourceAsStream("/data/cv.json");
        if (cvStream == null) {
            throw new RuntimeException("CV data not found at /data/cv.json");
        }

        profileData = objectMapper.readValue(cvStream, ProfileData.class);
        LOG.info("Loaded CV data for: " + profileData.getPersonalInfo().getName());
    }

    /**
     * Create vector embeddings and index CV data
     */
    private void indexCV() throws Exception {
        // Create semantic chunks from CV data
        List<DocumentChunk> chunks = createSemanticChunks(profileData);
        LOG.info("Created " + chunks.size() + " semantic chunks");

        // Generate embeddings for all chunks
        List<String> chunkTexts = chunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        List<float[]> embeddings = embeddingService.generateEmbeddings(chunkTexts);

        // Assign embeddings to chunks
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setEmbedding(embeddings.get(i));
        }

        // Store chunks in vector store
        vectorStoreService.addChunks(chunks);
        LOG.info("Indexed " + chunks.size() + " chunks in vector store");
    }

    /**
     * Load and index CV (combines both operations)
     */
    private void loadAndIndexCV() throws Exception {
        loadProfileData();
        indexCV();
    }

    /**
     * Create semantic chunks from profile data
     */
    private List<DocumentChunk> createSemanticChunks(ProfileData profile) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkId = 0;

        // Personal info and summary chunk
        String personalChunk = String.format(
                "Nom: %s\nTitre: %s\nEntreprise: %s\nLocalisation: %s\n\nRésumé: %s",
                profile.getPersonalInfo().getName(),
                profile.getPersonalInfo().getTitle(),
                profile.getPersonalInfo().getCompany(),
                profile.getPersonalInfo().getLocation(),
                profile.getSummary()
        );
        chunks.add(createChunk("chunk-" + chunkId++, personalChunk,
                Map.of("type", "personal", "name", profile.getPersonalInfo().getName())));

        // Experience chunks
        if (profile.getExperience() != null) {
            for (ProfileData.Experience exp : profile.getExperience()) {
                String expChunk = String.format(
                        "Expérience professionnelle:\nPoste: %s\nEntreprise: %s\nLocalisation: %s\nPériode: %s - %s\nDescription: %s",
                        exp.getTitle(),
                        exp.getCompany(),
                        exp.getLocation(),
                        exp.getStartDate() != null ? exp.getStartDate() : "Date non spécifiée",
                        exp.getEndDate(),
                        exp.getDescription()
                );
                chunks.add(createChunk("chunk-" + chunkId++, expChunk,
                        Map.of("type", "experience", "company", exp.getCompany(), "title", exp.getTitle())));
            }
        }

        // Education chunks
        if (profile.getEducation() != null) {
            for (ProfileData.Education edu : profile.getEducation()) {
                String eduChunk = String.format(
                        "Formation:\nÉtablissement: %s\nDomaine: %s\nPériode: %d - %d\nDescription: %s",
                        edu.getInstitution(),
                        edu.getField() != null ? edu.getField() : "Non spécifié",
                        edu.getStartYear(),
                        edu.getEndYear(),
                        edu.getDescription()
                );
                chunks.add(createChunk("chunk-" + chunkId++, eduChunk,
                        Map.of("type", "education", "institution", edu.getInstitution())));
            }
        }

        // Certifications chunk (grouped)
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) {
            StringBuilder certsBuilder = new StringBuilder("Certifications:\n");
            for (ProfileData.Certification cert : profile.getCertifications()) {
                certsBuilder.append(String.format(
                        "- %s (%s) - %s\n  %s\n",
                        cert.getName(),
                        cert.getIssuer() != null ? cert.getIssuer() : "Non spécifié",
                        cert.getDate(),
                        cert.getDescription()
                ));
            }
            chunks.add(createChunk("chunk-" + chunkId++, certsBuilder.toString(),
                    Map.of("type", "certifications")));
        }

        // Technical skills chunk
        if (profile.getSkills() != null && profile.getSkills().getTechnical() != null) {
            String skillsChunk = "Compétences techniques:\n" +
                    String.join(", ", profile.getSkills().getTechnical());
            chunks.add(createChunk("chunk-" + chunkId++, skillsChunk,
                    Map.of("type", "skills", "category", "technical")));
        }

        // Soft skills chunk
        if (profile.getSkills() != null && profile.getSkills().getSoft() != null) {
            String softSkillsChunk = "Compétences comportementales:\n" +
                    String.join(", ", profile.getSkills().getSoft());
            chunks.add(createChunk("chunk-" + chunkId++, softSkillsChunk,
                    Map.of("type", "skills", "category", "soft")));
        }

        // Languages chunk
        if (profile.getLanguages() != null && !profile.getLanguages().isEmpty()) {
            StringBuilder langsBuilder = new StringBuilder("Langues:\n");
            for (ProfileData.Language lang : profile.getLanguages()) {
                langsBuilder.append(String.format("- %s: %s\n", lang.getLanguage(), lang.getProficiency()));
            }
            chunks.add(createChunk("chunk-" + chunkId++, langsBuilder.toString(),
                    Map.of("type", "languages")));
        }

        // Projects chunk
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) {
            StringBuilder projectsBuilder = new StringBuilder("Projets:\n");
            for (ProfileData.Project project : profile.getProjects()) {
                projectsBuilder.append(String.format(
                        "- %s (%s)\n  %s\n",
                        project.getName(),
                        project.getUrl(),
                        project.getDescription()
                ));
            }
            chunks.add(createChunk("chunk-" + chunkId++, projectsBuilder.toString(),
                    Map.of("type", "projects")));
        }

        return chunks;
    }

    private DocumentChunk createChunk(String id, String content, Map<String, String> metadata) {
        DocumentChunk chunk = new DocumentChunk(id, content, metadata);
        return chunk;
    }

    /**
     * Process a user query using RAG
     */
    public Multi<String> processQuery(String userQuery) {
        LOG.info("Processing query: " + userQuery);

        // Search for relevant chunks
        List<DocumentChunk> relevantChunks = vectorStoreService.searchByText(userQuery, 5);

        if (relevantChunks.isEmpty()) {
            LOG.warn("No relevant chunks found for query");
            return claudeService.streamChatCompletion(
                    "You are an assistant representing Frédéric Klein, Senior Solution Architect at Red Hat. " +
                            "IMPORTANT: Always respond in the SAME LANGUAGE as the user's question (French if question is in French, English if in English).",
                    userQuery + "\n\n(Note: No specific information found in the CV for this question)"
            );
        }

        // Build context from relevant chunks
        String context = relevantChunks.stream()
                .map(chunk -> String.format("[Score: %.3f] %s", chunk.getScore(), chunk.getContent()))
                .collect(Collectors.joining("\n\n---\n\n"));

        LOG.info("Found " + relevantChunks.size() + " relevant chunks");

        // Build system prompt with context
        String systemPrompt = String.format(
                "You are an assistant representing Frédéric Klein, Senior Solution Architect at Red Hat. " +
                        "You answer questions professionally and concisely using information from his CV.\n\n" +
                        "CV CONTEXT:\n%s\n\n" +
                        "Instructions:\n" +
                        "- IMPORTANT: Always respond in the SAME LANGUAGE as the user's question (French if question is in French, English if in English)\n" +
                        "- Use the context information to answer precisely\n" +
                        "- If the question is about Frédéric, respond in first person\n" +
                        "- If the information is not in the context, say so honestly\n" +
                        "- Keep responses concise (2-3 paragraphs maximum)",
                context
        );

        // Stream response from Claude
        return claudeService.streamChatCompletion(systemPrompt, userQuery);
    }

    /**
     * Get profile data
     */
    public ProfileData getProfileData() {
        return profileData;
    }

    /**
     * Reload and reindex the CV (useful for updates)
     */
    public void reindexCV() {
        try {
            vectorStoreService.clear();
            loadAndIndexCV();
            LOG.info("CV reindexed successfully");
        } catch (Exception e) {
            LOG.error("Failed to reindex CV", e);
            throw new RuntimeException("Failed to reindex CV", e);
        }
    }
}
