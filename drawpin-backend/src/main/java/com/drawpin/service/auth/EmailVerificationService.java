package com.drawpin.service.auth;

import com.drawpin.domain.entity.EmailVerification;
import com.drawpin.exception.ValidationException;
import com.drawpin.repository.EmailVerificationRepository;
import com.drawpin.repository.UserRepository;
import com.drawpin.util.HashUtils;
import com.drawpin.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the email verification token lifecycle.
 *
 * <p><b>Flow:</b>
 * <ol>
 *   <li>On registration, {@link AuthService} calls {@link #sendVerificationEmail} to create
 *       a token and dispatch the verification email asynchronously.</li>
 *   <li>The user clicks the link, the frontend sends the raw token to
 *       {@code POST /api/v1/auth/verify-email}.</li>
 *   <li>{@link #verify(String)} hashes the token, looks it up, validates it,
 *       marks the token as used, and sets {@code users.email_verified = true}.</li>
 * </ol>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link EmailVerificationRepository} — token persistence</li>
 *   <li>{@link UserRepository} — marks email as verified</li>
 *   <li>{@link EmailService} — sends the email</li>
 *   <li>{@link AuthService} — calls {@link #sendVerificationEmail} on registration</li>
 *   <li>{@link com.drawpin.controller.auth.AuthController#verifyEmail} — entry point</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${drawpin.email-verification-expiry-hours:24}")
    private int expiryHours;

    /**
     * Creates a verification token and sends the verification email.
     *
     * <p>Called after a new user is persisted during registration.
     * Email sending is async — this method returns immediately.
     *
     * @param userId      the newly registered user's UUID
     * @param userEmail   the email address to verify
     * @param displayName the user's name for email personalisation
     */
    public void sendVerificationEmail(UUID userId, String userEmail, String displayName) {
        String rawToken = TokenUtils.generate();
        String tokenHash = HashUtils.sha256(rawToken);

        EmailVerification verification = EmailVerification.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusSeconds((long) expiryHours * 3600))
                .build();

        emailVerificationRepository.save(verification);

        // Fire-and-forget — does not block the HTTP response
        emailService.sendVerificationEmail(userEmail, displayName, rawToken);
        log.debug("Verification email queued for user {}", userId);
    }

    /**
     * Validates the submitted verification token and marks the user's email as verified.
     *
     * @param rawToken the raw token from the email link query parameter
     * @throws ValidationException if the token is not found, expired, or already used
     */
    public void verify(String rawToken) {
        String tokenHash = HashUtils.sha256(rawToken);

        EmailVerification verification = emailVerificationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ValidationException(
                        "INVALID_VERIFICATION_TOKEN", "Verification link is invalid or has expired."));

        if (!verification.isValid()) {
            String reason = verification.isExpired() ? "expired" : "already used";
            log.warn("Verification token {} is {}", rawToken.substring(0, 8) + "...", reason);
            throw new ValidationException(
                    "VERIFICATION_TOKEN_INVALID",
                    "This verification link has already been used or has expired. Please request a new one.");
        }

        // Mark the token as verified
        verification.setVerifiedAt(Instant.now());
        emailVerificationRepository.save(verification);

        // Fetch user, apply pending email if it exists
        com.drawpin.domain.entity.User user = userRepository.findById(verification.getUserId())
            .orElseThrow(() -> new ValidationException("USER_NOT_FOUND", "User no longer exists"));
        
        if (user.getPendingEmail() != null) {
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
            log.info("Email updated from pending email for user {}", user.getId());
        }
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user {}", verification.getUserId());
    }
}
