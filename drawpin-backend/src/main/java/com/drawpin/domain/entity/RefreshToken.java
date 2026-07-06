package com.drawpin.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a valid JWT refresh token session.
 *
 * <p>One row exists per active device session for a user. When a user logs in from
 * multiple devices (phone, laptop, work computer), each gets its own refresh token row.
 * The {@code token_hash} column stores the SHA-256 hash of the raw token — the raw
 * token is only ever returned once to the client in an HttpOnly cookie and never persisted
 * in plain text.
 *
 * <p>Tokens are additionally mirrored in Redis for O(1) validity checks on every
 * {@code /auth/refresh} call, with PostgreSQL serving as the source of truth.
 *
 * <p><b>Table:</b> {@code refresh_tokens}<br>
 * <b>Managed by Flyway:</b> {@code V2__create_auth_tokens.sql}
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link User} — many-to-one FK on {@code user_id}</li>
 *   <li>{@link com.drawpin.service.auth.RefreshTokenService} — issues, rotates, and revokes</li>
 *   <li>{@link com.drawpin.repository.RefreshTokenRepository}</li>
 * </ul>
 *
 * <p><b>Frontend APIs that consume this entity:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/login} — creates a refresh token</li>
 *   <li>{@code POST /api/v1/auth/refresh} — rotates the refresh token</li>
 *   <li>{@code POST /api/v1/auth/logout} — revokes the refresh token</li>
 *   <li>{@code GET /api/v1/auth/sessions} — lists active sessions</li>
 *   <li>{@code DELETE /api/v1/auth/sessions/{id}} — revokes a specific session</li>
 * </ul>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** FK reference to the owning user. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * SHA-256 hash of the raw refresh token string.
     * The raw token is never stored; only this hash is persisted.
     */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /** Browser and OS string parsed from the User-Agent header at login time. */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    /** IP address of the client at login time. Stored for audit purposes. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** The exact moment this token expires. Checked during refresh validation. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * When {@code true}, this token has been explicitly revoked.
     * Revoked tokens cannot be used for refresh even if not yet expired.
     */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ─────────────────────────────────────────────────────────────────────────
    // DOMAIN HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if this token is still valid — not revoked and not expired.
     *
     * @return {@code true} if the token can be used for a refresh operation
     */
    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}
