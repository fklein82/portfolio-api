package com.fklein.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Get origin from request
        String origin = requestContext.getHeaderString("Origin");

        // In production, only allow specific origins
        if ("prod".equals(profile)) {
            if (origin != null &&
                (origin.equals("https://fklein82.github.io") ||
                 origin.equals("http://localhost:8000"))) {
                responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            }
        } else {
            // In dev mode, allow all origins
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin != null ? origin : "*");
        }

        responseContext.getHeaders().add("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization, x-requested-with");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "false");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }
}
