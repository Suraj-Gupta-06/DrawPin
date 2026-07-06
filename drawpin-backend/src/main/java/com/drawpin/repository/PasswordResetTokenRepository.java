package com.drawpin.repository;

import com.drawpin.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PasswordResetToken}.
 *
 * <p>Used exclusively by {@link com.drawpin.service.auth.PasswordResetService}
 * to issue, validate, consume, and rate-limit password reset tokens.
 *
 * <p><b>Frontend APIs that trigger operations here:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/forgot-password} — issues a new token</li>
 *   <li>{@code POST /api/v1/auth/reset-password} — validates and consumes the token</li>
 * </ul>
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Looks up a password reset token by its SHA-256 hash.
     * The raw token from the email link is hashed before this call.
     *
     * @param tokenHash SHA-256 hash of the raw token
     * @return an {@link Optional} containing the token if found
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Finds the most recent token issued for a user, regardless of its state.
     * Used for rate-limiting: if the most recent token was issued within the last
     * N minutes, a new one should not be sent.
     *
     * @param userId the user's UUID
     * @return an {@link Optional} containing the latest token if any exists
     */
    @Query("""
            SELECT prt FROM PasswordResetToken prt
            WHERE prt.userId = :userId
            ORDER BY prt.createdAt DESC
            LIMIT 1
            """)
    Optional<PasswordResetToken> findLatestByUserId(@Param("userId") UUID userId);

    /**
     * Marks all existing password reset tokens for a user as used.
     * Ensures only one valid token exists at a time.
     *
     * @param userId the user's UUID
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = TRUE WHERE prt.userId = :userId AND prt.used = FALSE")
    void invalidateAllForUser(@Param("userId") UUID userId);

    /**
     * Deletes all expired and used tokens from the table (for cleanup jobs).
     *
     * @param now tokens with {@code expires_at} before this instant are removed
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now OR prt.used = TRUE")
    void deleteExpiredAndUsed(@Param("now") Instant now);
}
