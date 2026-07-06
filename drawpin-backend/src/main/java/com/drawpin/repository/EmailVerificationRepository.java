package com.drawpin.repository;

import com.drawpin.domain.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link EmailVerification}.
 *
 * <p>Used exclusively by {@link com.drawpin.service.auth.EmailVerificationService}
 * to issue and validate email verification tokens after user registration.
 *
 * <p><b>Frontend APIs that trigger operations here:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — indirectly triggers token creation</li>
 *   <li>{@code POST /api/v1/auth/verify-email} — validates and consumes the token</li>
 * </ul>
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    /**
     * Looks up an email verification token by its SHA-256 hash.
     *
     * @param tokenHash SHA-256 hash of the raw token from the email link
     * @return an {@link Optional} containing the verification record if found
     */
    Optional<EmailVerification> findByTokenHash(String tokenHash);

    /**
     * Deletes all expired or already-verified tokens (for scheduled cleanup).
     *
     * @param now tokens with {@code expires_at} before this instant are removed
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now OR ev.verifiedAt IS NOT NULL")
    void deleteExpiredAndVerified(@Param("now") Instant now);
}
