package com.drawpin.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for one-time password reset tokens.
 *
 * <p>When a user submits the forgot-password form, a cryptographically random
 * token is generated, hashed with SHA-256, and stored here. The raw token is
 * sent via email. On the reset-password page the user submits the raw token,
 * which the server re-hashes and looks up against this table.
 *
 * <p><b>Security properties:</b>
 * <ul>
 *   <li>Tokens expire in 1 hour ({@code expires_at})</li>
 *   <li>Tokens are single-use ({@code used = true} after use)</li>
 *   <li>Rate limited to 1 issuance per 5 minutes per email (enforced in service)</li>
 *   <li>Always returns 200 on the forgot-password endpoint (prevents email enumeration)</li>
 * </ul>
 *
 * <p><b>Table:</b> {@code password_reset_tokens}<br>
 * <b>Managed by Flyway:</b> {@code V2__create_auth_tokens.sql}
 *
 * <p><b>Frontend APIs that consume this entity:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/forgot-password} — creates the token</li>
 *   <li>{@code POST /api/v1/auth/reset-password} — validates and consumes the token</li>
 * </ul>
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** SHA-256 hash of the raw reset token sent in the email link. */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /** Exact expiry time — 1 hour from creation by default. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Set to {@code true} after the token is used to prevent re-use. */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ─────────────────────────────────────────────────────────────────────────
    // DOMAIN HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns {@code true} if the token is still within its validity window. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Returns {@code true} if the token can still be used (not used and not expired). */
    public boolean isValid() {
        return !used && !isExpired();
    }
}
