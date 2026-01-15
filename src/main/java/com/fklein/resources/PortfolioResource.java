package com.fklein.resources;

import com.fklein.models.ProfileData;
import com.fklein.services.RagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class PortfolioResource {

    @Inject
    RagService ragService;

    /**
     * Get profile data as JSON
     */
    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public ProfileData getProfile() {
        return ragService.getProfileData();
    }
}
