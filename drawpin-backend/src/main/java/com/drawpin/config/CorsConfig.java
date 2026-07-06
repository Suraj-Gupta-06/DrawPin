package com.drawpin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the DrawPin API.
 *
 * <p>Allows the React frontend (Vite dev server on port 5173, production domain)
 * to make cross-origin requests with credentials (cookies for refresh tokens).
 *
 * <p><b>Key settings:</b>
 * <ul>
 *   <li>{@code allowedOrigins} — only the configured frontend URL (never {@code *} when
 *       {@code allowCredentials} is true)</li>
 *   <li>{@code allowCredentials = true} — required for HttpOnly cookie refresh tokens</li>
 *   <li>{@code allowedHeaders} — standard headers plus {@code Authorization}</li>
 *   <li>{@code exposedHeaders} — empty; the frontend only reads the body, not response headers</li>
 * </ul>
 *
 * <p>This bean is referenced by {@link SecurityConfig} to apply CORS before the
 * Spring Security filter chain processes the request.
 */
@Configuration
public class CorsConfig {

    @Value("${drawpin.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Creates the CORS configuration applied to all {@code /**} routes.
     *
     * @return a {@link CorsConfigurationSource} applied to every request
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow the configured frontend origin — wildcards break credential cookies
        config.setAllowedOrigins(List.of(frontendUrl));

        // Allow all standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Required headers for JSON + JWT auth
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Required for HttpOnly refresh token cookie to be sent automatically
        config.setAllowCredentials(true);

        // Preflight cache duration (1 hour)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
