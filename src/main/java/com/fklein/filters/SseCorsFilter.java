package com.fklein.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

@Provider
public class SseCorsFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(SseCorsFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        String origin = requestContext.getHeaderString("Origin");

        LOG.info("SseCorsFilter - Origin: " + origin + ", Path: " + requestContext.getUriInfo().getPath());

        // Always add CORS headers
        if (origin != null) {
            headers.putSingle("Access-Control-Allow-Origin", origin);
        } else {
            headers.putSingle("Access-Control-Allow-Origin", "*");
        }

        headers.putSingle("Access-Control-Allow-Credentials", "false");
        headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        headers.putSingle("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        headers.putSingle("Access-Control-Expose-Headers", "*");
        headers.putSingle("Access-Control-Max-Age", "3600");

        LOG.info("CORS headers added to response");
    }
}
