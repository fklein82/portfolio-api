package com.fklein.config;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

@ApplicationScoped
public class VertxCorsHandler {

    private static final Logger LOG = Logger.getLogger(VertxCorsHandler.class);

    public void registerFilter(@Observes Filters filters) {
        LOG.info("Registering Vert.x CORS filter");

        filters.register(rc -> {
            HttpServerRequest request = rc.request();
            HttpServerResponse response = rc.response();

            String origin = request.getHeader("Origin");
            String path = request.path();

            LOG.info("Vert.x CORS filter - Path: " + path + ", Origin: " + origin);

            // Add CORS headers to every response
            if (origin != null && !origin.isEmpty()) {
                response.putHeader("Access-Control-Allow-Origin", origin);
            } else {
                response.putHeader("Access-Control-Allow-Origin", "*");
            }

            response.putHeader("Access-Control-Allow-Credentials", "false");
            response.putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            response.putHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
            response.putHeader("Access-Control-Expose-Headers", "*");
            response.putHeader("Access-Control-Max-Age", "3600");

            // Handle preflight
            if ("OPTIONS".equals(request.method().name())) {
                LOG.info("Handling OPTIONS preflight request");
                response.setStatusCode(200).end();
            } else {
                LOG.info("Added CORS headers, continuing to next handler");
                rc.next();
            }
        }, 10); // Priority 10 to run early
    }
}
