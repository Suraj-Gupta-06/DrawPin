package com.drawpin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger UI configuration.
 *
 * <p>Provides the full API specification at {@code /api/v1/v3/api-docs} and
 * the interactive Swagger UI at {@code /api/v1/swagger-ui.html}.
 *
 * <p>The Bearer authentication scheme is registered globally so every endpoint
 * shows a lock icon and the "Authorize" button populates the JWT header for
 * all subsequent test calls.
 *
 * <p><b>Access in development:</b>
 * <a href="http://localhost:8080/api/v1/swagger-ui.html">http://localhost:8080/api/v1/swagger-ui.html</a>
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${drawpin.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Defines the OpenAPI specification metadata, server list, and global security scheme.
     *
     * @return fully configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI drawPinOpenApi() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Local Development"),
                        new Server().url("https://api.drawpin.com/api/v1").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, bearerSecurityScheme())
                );
    }

    /**
     * Builds the API metadata block shown at the top of the Swagger UI.
     *
     * @return API {@link Info}
     */
    private Info apiInfo() {
        return new Info()
                .title("DrawPin 2.0 API")
                .description("""
                        DrawPin 2.0 — Creative Marketplace Platform REST API.
                        
                        All endpoints return a standard `ApiResponse<T>` envelope with a `success` flag.
                        Authenticated endpoints require a JWT Bearer token obtained from `POST /auth/login`.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("DrawPin Engineering")
                        .email("api@drawpin.com")
                        .url("https://drawpin.com"))
                .license(new License().name("Proprietary").url("https://drawpin.com/terms"));
    }

    /**
     * Defines the JWT Bearer authentication scheme used by all protected endpoints.
     *
     * @return the {@link SecurityScheme} for Bearer tokens
     */
    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(BEARER_SCHEME)
                .description("JWT access token from POST /auth/login. Format: Bearer {token}");
    }
}
