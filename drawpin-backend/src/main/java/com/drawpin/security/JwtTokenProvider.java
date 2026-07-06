package com.drawpin.security;

import com.drawpin.config.JwtConfig;
import com.drawpin.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Provides JWT access token generation and validation.
 *
 * <p>This component is the single source of truth for all JWT operations.
 * It uses HMAC-SHA512 signing via the JJWT library with a 512-bit secret
 * derived from the configured {@code drawpin.jwt.secret} property.
 *
 * <p><b>Token claims structure:</b>
 * <pre>
 * Header: { "alg": "HS512", "typ": "JWT" }
 * Payload: {
 *   "sub":   "550e8400-e29b-41d4-a716-446655440000",   // user UUID
 *   "email": "aria@example.com",
 *   "role":  "CREATOR",
 *   "iat":   1720000000,
 *   "exp":   1720000900                                 // +15 min
 * }
 * </pre>
 *
 * <p><b>Security design:</b>
 * <ul>
 *   <li>The secret must be at least 512 bits (64 chars) in production.</li>
 *   <li>Access tokens are stateless and expire in 15 minutes.</li>
 *   <li>Refresh tokens are stored in the DB (not JWT) to enable revocation.</li>
 * </ul>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link JwtAuthenticationFilter} — validates tokens on every request</li>
 *   <li>{@link com.drawpin.service.auth.JwtService} — delegates generation here</li>
 *   <li>{@link JwtConfig} — reads expiry and secret from application.yml</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    /** Derived signing key — initialised once on startup from the configured secret. */
    private SecretKey signingKey;

    /**
     * Initialises the HMAC-SHA512 signing key from the configured secret string.
     * Runs once after the bean is fully constructed.
     */
    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        log.info("JWT signing key initialised with HMAC-SHA512");
    }

    /**
     * Generates a signed JWT access token for the given user.
     *
     * <p>The token embeds the user's UUID (as {@code sub}), email, and role as claims.
     * It expires in {@code drawpin.jwt.access-token-expiry-seconds} seconds (default 900).
     *
     * @param user the authenticated user
     * @return a compact JWT string (header.payload.signature)
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtConfig.getAccessTokenExpirySeconds());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extracts the user UUID from a validated JWT token.
     *
     * @param token a compact JWT string
     * @return the user UUID stored in the {@code sub} claim
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    /**
     * Extracts the email address from a validated JWT token.
     *
     * @param token a compact JWT string
     * @return the email stored in the {@code email} claim
     */
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Validates a JWT token's signature and expiry.
     *
     * <p>Returns a typed {@link JwtValidationResult} instead of throwing exceptions,
     * so the filter can log a specific message and return the appropriate HTTP error.
     *
     * @param token a compact JWT string
     * @return a result indicating validity or the type of failure
     */
    public JwtValidationResult validate(String token) {
        try {
            getClaims(token);
            return JwtValidationResult.VALID;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return JwtValidationResult.EXPIRED;
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
            return JwtValidationResult.INVALID_SIGNATURE;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            return JwtValidationResult.MALFORMED;
        }
    }

    /**
     * Returns the configured access token TTL in seconds.
     * Embedded in the {@link com.drawpin.dto.response.AuthResponse} as {@code expiresIn}.
     *
     * @return the access token lifetime in seconds
     */
    public long getAccessTokenExpirySeconds() {
        return jwtConfig.getAccessTokenExpirySeconds();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parses and validates the JWT, returning its claims body.
     *
     * @param token a compact JWT string
     * @return the parsed {@link Claims}
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Typed result of JWT validation to avoid exception-driven control flow in the filter. */
    public enum JwtValidationResult {
        VALID,
        EXPIRED,
        INVALID_SIGNATURE,
        MALFORMED
    }
}
