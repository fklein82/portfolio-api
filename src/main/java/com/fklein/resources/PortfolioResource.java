package com.fklein.resources;

import com.fklein.models.ProfileData;
import com.fklein.services.RagService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class PortfolioResource {

    @Inject
    RagService ragService;

    @Inject
    Template index;

    /**
     * Serve the main portfolio page
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance homepage() {
        ProfileData profile = ragService.getProfileData();
        return index.data("profile", profile);
    }

    /**
     * Get profile data as JSON
     */
    @GET
    @Path("/api/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public ProfileData getProfile() {
        return ragService.getProfileData();
    }
}
