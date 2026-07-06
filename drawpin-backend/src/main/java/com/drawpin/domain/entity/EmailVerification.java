package com.drawpin.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for email address verification tokens.
 *
 * <p>On successful registration, a verification email is sent asynchronously. The raw token
 * is embedded in the link. When the user clicks the link, the frontend calls
 * {@code POST /api/v1/auth/verify-email} with the token. The server hashes it and
 * looks it up in this table. On match, {@code users.email_verified} is set to {@code true}.
 *
 * <p><b>Table:</b> {@code email_verifications}<br>
 * <b>Managed by Flyway:</b> {@code V2__create_auth_tokens.sql}
 *
 * <p><b>Frontend APIs that consume this entity:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — triggers token creation</li>
 *   <li>{@code POST /api/v1/auth/verify-email} — validates and marks verified</li>
 * </ul>
 */
@Entity
@Table(name = "email_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** SHA-256 hash of the raw verification token sent in the email. */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /** Expiry — 24 hours from creation by default. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Set to the current timestamp when the user successfully verifies their email.
     * Null means the email has not been verified yet.
     */
    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ─────────────────────────────────────────────────────────────────────────
    // DOMAIN HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns {@code true} if the token has expired. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Returns {@code true} if this token has already been used to verify an email. */
    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    /** Returns {@code true} if the token is still valid and can be consumed. */
    public boolean isValid() {
        return !isExpired() && !isAlreadyVerified();
    }
}
