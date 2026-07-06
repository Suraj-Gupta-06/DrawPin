package com.drawpin.repository;

import com.drawpin.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RefreshToken}.
 *
 * <p>All refresh token lifecycle operations — lookup by hash, revocation, expiry cleanup,
 * and session listing — are defined here.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.RefreshTokenService} — the sole consumer</li>
 * </ul>
 *
 * <p><b>Frontend APIs that trigger operations here:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}</li>
 *   <li>{@code POST /api/v1/auth/refresh}</li>
 *   <li>{@code POST /api/v1/auth/logout}</li>
 *   <li>{@code GET /api/v1/auth/sessions}</li>
 *   <li>{@code DELETE /api/v1/auth/sessions/{id}}</li>
 * </ul>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Looks up a refresh token by its SHA-256 hash.
     * This is the primary lookup used during the token rotation flow.
     *
     * @param tokenHash SHA-256 hash of the raw token from the cookie
     * @return an {@link Optional} containing the token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Lists all valid (not revoked, not expired) sessions for a given user.
     * Used by {@code GET /api/v1/auth/sessions} to show active devices.
     *
     * @param userId    the user's UUID
     * @param now       the current instant — tokens expiring before this are excluded
     * @return list of active refresh tokens
     */
    @Query("""
            SELECT rt FROM RefreshToken rt
            WHERE rt.userId = :userId
              AND rt.revoked = FALSE
              AND rt.expiresAt > :now
            ORDER BY rt.createdAt DESC
            """)
    List<RefreshToken> findValidSessionsByUserId(@Param("userId") UUID userId,
                                                 @Param("now") Instant now);

    /**
     * Revokes all refresh tokens for a user.
     * Called during password reset, account deletion, and forced logout-all.
     *
     * @param userId the user's UUID
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = TRUE WHERE rt.userId = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    /**
     * Revokes a specific token by its hash.
     * Called during normal logout.
     *
     * @param tokenHash SHA-256 hash of the token to revoke
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = TRUE WHERE rt.tokenHash = :tokenHash")
    void revokeByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Deletes all expired tokens from the table.
     * Invoked by a scheduled cleanup job (not in the auth module — defined in Phase 10).
     *
     * @param now tokens with {@code expires_at} before this instant are deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}
