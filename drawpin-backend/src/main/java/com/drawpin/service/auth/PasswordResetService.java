package com.drawpin.service.auth;

import com.drawpin.domain.entity.PasswordResetToken;
import com.drawpin.domain.entity.User;
import com.drawpin.exception.RateLimitException;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.exception.ValidationException;
import com.drawpin.repository.PasswordResetTokenRepository;
import com.drawpin.repository.UserRepository;
import com.drawpin.util.HashUtils;
import com.drawpin.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Manages the password reset flow (forgot password).
 *
 * <p><b>Initiation flow ({@code POST /auth/forgot-password}):</b>
 * <ol>
 *   <li>Look up the user by email. If not found, respond 200 silently (prevents enumeration).</li>
 *   <li>Check rate limit: reject if a token was issued in the last N minutes.</li>
 *   <li>Invalidate all existing tokens for this user.</li>
 *   <li>Generate a new raw token and persist its hash.</li>
 *   <li>Send the reset email asynchronously.</li>
 * </ol>
 *
 * <p><b>Reset flow ({@code POST /auth/reset-password}):</b>
 * <ol>
 *   <li>Hash the submitted raw token and look it up.</li>
 *   <li>Validate: not used, not expired.</li>
 *   <li>Mark the token as used.</li>
 *   <li>BCrypt-hash and save the new password.</li>
 *   <li>Revoke all refresh tokens (force re-login from all devices).</li>
 *   <li>Send a "password changed" security notification email.</li>
 * </ol>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link PasswordResetTokenRepository} — token persistence</li>
 *   <li>{@link UserRepository} — load user, update password</li>
 *   <li>{@link RefreshTokenService} — revoke all sessions on reset</li>
 *   <li>{@link EmailService} — send reset and confirmation emails</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${drawpin.password-reset-expiry-hours:1}")
    private int expiryHours;

    @Value("${drawpin.password-reset-rate-limit-minutes:5}")
    private int rateLimitMinutes;

    /**
     * Initiates a password reset by generating a token and sending a reset email.
     *
     * <p>Always returns without throwing an exception when the email is not found —
     * this prevents attackers from using the endpoint to enumerate registered emails.
     *
     * @param email the email address submitted in the forgot-password form
     */
    public void initiateReset(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            // Check rate limit — prevent spam
            passwordResetTokenRepository.findLatestByUserId(user.getId()).ifPresent(latest -> {
                boolean tooSoon = latest.getCreatedAt()
                        .isAfter(Instant.now().minus(rateLimitMinutes, ChronoUnit.MINUTES));
                if (tooSoon) {
                    throw new RateLimitException(
                            "RESET_EMAIL_RATE_LIMIT",
                            "A password reset email was recently sent. Please wait " + rateLimitMinutes +
                            " minutes before requesting another.");
                }
            });

            // Invalidate any previous tokens for clean state
            passwordResetTokenRepository.invalidateAllForUser(user.getId());

            // Generate and persist new token
            String rawToken = TokenUtils.generate();
            PasswordResetToken token = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(HashUtils.sha256(rawToken))
                    .expiresAt(Instant.now().plusSeconds((long) expiryHours * 3600))
                    .build();
            passwordResetTokenRepository.save(token);

            // Fire-and-forget email dispatch
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), rawToken);
            log.info("Password reset token issued for user {}", user.getId());
        });
        // No exception if email not found — silent 200 response prevents enumeration
    }

    /**
     * Validates the reset token and applies the new password.
     *
     * @param rawToken    the raw token from the email link
     * @param newPassword the new password in plain text (will be BCrypt-hashed)
     * @throws ValidationException if the token is invalid, expired, or already used
     */
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = HashUtils.sha256(rawToken);

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ValidationException(
                        "INVALID_RESET_TOKEN", "Password reset link is invalid or has expired."));

        if (!token.isValid()) {
            log.warn("Attempted use of invalid password reset token");
            throw new ValidationException(
                    "RESET_TOKEN_INVALID", "This reset link has already been used or has expired.");
        }

        // Load the user
        User user = userRepository.findByIdAndDeletedAtIsNull(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND", "Associated user account not found."));

        // Mark token as consumed
        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        // Update the password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Force logout from all devices for security
        refreshTokenService.revokeAllTokensForUser(user.getId());

        // Security notification
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());
        log.info("Password reset completed for user {}", user.getId());
    }
}
