package com.drawpin.config;

import com.drawpin.security.DrawPinUserDetailsService;
import com.drawpin.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for DrawPin.
 *
 * <p><b>Security model:</b>
 * <ul>
 *   <li>Stateless JWT authentication — no server-side sessions</li>
 *   <li>CSRF disabled (safe for stateless JWT APIs with same-origin SPA)</li>
 *   <li>CORS configured via {@link CorsConfig}</li>
 *   <li>Method-level security via {@code @PreAuthorize} / {@code @PostAuthorize}</li>
 * </ul>
 *
 * <p><b>Public endpoints (no token required):</b>
 * <ul>
 *   <li>All {@code /auth/**} endpoints (register, login, refresh, etc.)</li>
 *   <li>Swagger UI and API docs</li>
 *   <li>GET requests on pins, creators, categories (public browse)</li>
 *   <li>Actuator health endpoint</li>
 * </ul>
 *
 * <p><b>Protected endpoints:</b> Everything else requires a valid JWT Bearer token.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link JwtAuthenticationFilter} — added before the form-login filter</li>
 *   <li>{@link DrawPinUserDetailsService} — the authentication provider's user source</li>
 *   <li>{@link CorsConfig} — provides the CORS configuration source</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final DrawPinUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    /** Public paths that do not require authentication. */
    private static final String[] PUBLIC_POST_PATHS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/verify-email"
    };

    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /**
     * Defines the HTTP security filter chain — the core security policy.
     *
     * @param http Spring Security's HTTP security builder
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF disabled: stateless JWT API, not browser form submissions
                .csrf(AbstractHttpConfigurer::disable)

                // Apply CORS configuration from CorsConfig bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless session — no HttpSession created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        // Health check
                        .requestMatchers("/actuator/health").permitAll()
                        // Public auth endpoints (POST only)
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
                        // Public read-only browse endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/pins/**",
                                "/api/v1/creators/**",
                                "/api/v1/categories/**",
                                "/api/v1/media/**"
                        ).permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Custom authentication provider (BCrypt + UserDetailsService)
                .authenticationProvider(authenticationProvider())

                // Insert JWT filter before Spring's built-in username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * Creates a DAO-based authentication provider that uses BCrypt for password
     * verification and the DrawPin user details service for user lookup.
     *
     * @return configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} bean for use in {@link com.drawpin.service.auth.AuthService}.
     *
     * @param config Spring's authentication configuration
     * @return the application's authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder with strength factor 12.
     * Strength 12 provides ~300ms hashing time — resistant to brute force while
     * being acceptable for login latency.
     *
     * @return the {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
