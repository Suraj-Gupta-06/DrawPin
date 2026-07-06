package com.drawpin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Strongly-typed configuration properties for JWT settings.
 *
 * <p>Bound from the {@code drawpin.jwt.*} namespace in {@code application.yml}.
 * Validated at startup — the application will not start if the secret is missing
 * or too short, preventing insecure deployments.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.security.JwtTokenProvider} — reads secret and expiry</li>
 *   <li>{@link com.drawpin.service.auth.RefreshTokenService} — reads refresh expiry</li>
 * </ul>
 */
@Configuration
@ConfigurationProperties(prefix = "drawpin.jwt")
@Data
@Validated
public class JwtConfig {

    /**
     * HMAC-SHA512 signing secret. Must be at least 64 characters (512 bits) for HS512.
     * Set via the {@code JWT_SECRET} environment variable in production.
     */
    @NotBlank(message = "JWT secret must not be blank")
    @Size(min = 64, message = "JWT secret must be at least 64 characters for HS512 security")
    private String secret;

    /**
     * Access token TTL in seconds. Default: 900 (15 minutes).
     * Short expiry minimises the window of token misuse if intercepted.
     */
    @Min(value = 60, message = "Access token expiry must be at least 60 seconds")
    private long accessTokenExpirySeconds = 900L;

    /**
     * Standard refresh token TTL in seconds. Default: 1,209,600 (14 days).
     */
    @Min(value = 3600, message = "Refresh token expiry must be at least 1 hour")
    private long refreshTokenExpirySeconds = 1_209_600L;

    /**
     * Extended refresh token TTL for "Remember me" sessions. Default: 2,592,000 (30 days).
     */
    @Min(value = 86400, message = "Remember-me expiry must be at least 1 day")
    private long rememberMeExpirySeconds = 2_592_000L;
}
