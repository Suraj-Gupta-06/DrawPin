package com.drawpin.service.auth;

import com.drawpin.domain.entity.User;
import com.drawpin.domain.entity.UserSettings;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.domain.enums.UserStatus;
import com.drawpin.dto.request.auth.*;
import com.drawpin.dto.response.AuthResponse;
import com.drawpin.dto.response.AuthResult;
import com.drawpin.dto.response.UserResponse;
import com.drawpin.exception.ConflictException;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.exception.UnauthorizedException;
import com.drawpin.exception.ValidationException;
import com.drawpin.repository.UserRepository;
import com.drawpin.repository.UserSettingsRepository;
import com.drawpin.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Core authentication service — the orchestrator for all auth operations.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Register new users (create User + UserSettings, generate handle, send verification email)</li>
 *   <li>Login (validate credentials, check account status, issue tokens)</li>
 *   <li>Refresh access tokens via rotating refresh tokens</li>
 *   <li>Logout (revoke refresh token)</li>
 *   <li>Change password for authenticated users</li>
 *   <li>Delete account (soft delete)</li>
 * </ul>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController} — the entry point for all auth requests</li>
 *   <li>{@link JwtService} — generates access tokens and AuthResponse</li>
 *   <li>{@link RefreshTokenService} — manages refresh token lifecycle</li>
 *   <li>{@link EmailVerificationService} — sends verification emails</li>
 *   <li>{@link EmailService} — sends password change notifications</li>
 *   <li>{@link UserRepository} — user persistence</li>
 *   <li>{@link UserSettingsRepository} — settings persistence</li>
 * </ul>
 *
 * <p><b>Frontend APIs consumed:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register}</li>
 *   <li>{@code POST /api/v1/auth/login}</li>
 *   <li>{@code POST /api/v1/auth/refresh}</li>
 *   <li>{@code POST /api/v1/auth/logout}</li>
 *   <li>{@code POST /api/v1/auth/change-password}</li>
 *   <li>{@code DELETE /api/v1/auth/account}</li>
 *   <li>{@code GET /api/v1/users/me}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${drawpin.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${drawpin.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new user account.
     *
     * <p><b>Steps:</b>
     * <ol>
     *   <li>Check email uniqueness — throw {@link ConflictException} if taken</li>
     *   <li>Generate a unique URL-safe handle from the display name</li>
     *   <li>BCrypt-hash the password</li>
     *   <li>Persist the User entity</li>
     *   <li>Create default UserSettings</li>
     *   <li>Send verification email asynchronously</li>
     *   <li>Issue refresh token and return AuthResult</li>
     * </ol>
     *
     * @param request    the validated registration payload
     * @param deviceInfo the User-Agent string for session tracking
     * @param ipAddress  the client IP for session tracking
     * @return {@link AuthResult} containing the access token, user profile, and raw refresh token
     */
    public AuthResult register(RegisterRequest request, String deviceInfo, String ipAddress) {
        // Uniqueness check
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS",
                    "An account with this email address already exists.");
        }

        // Generate unique handle
        String baseHandle = SlugUtils.toHandle(request.getName());
        String handle = generateUniqueHandle(baseHandle);

        // Determine role — only COLLECTOR and CREATOR are allowed at registration
        UserRole role = (request.getRole() == UserRole.CREATOR) ? UserRole.CREATOR : UserRole.COLLECTOR;

        // Create and persist user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .handle(handle)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        final UUID userId = user.getId();

        // Create default settings
        UserSettings settings = UserSettings.builder()
                .userId(userId)
                .build();
        userSettingsRepository.save(settings);

        // Send verification email (async — does not block response)
        emailVerificationService.sendVerificationEmail(userId, user.getEmail(), user.getName());

        // Issue refresh token — raw token returned to controller to set as HttpOnly cookie
        String rawRefreshToken = refreshTokenService.issueToken(userId, deviceInfo, ipAddress, false);

        log.info("New user registered: {} ({}) as {}", user.getEmail(), user.getHandle(), role);
        return new AuthResult(jwtService.buildAuthResponse(user, toUserResponse(user)), rawRefreshToken);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user with email and password.
     *
     * <p><b>Steps:</b>
     * <ol>
     *   <li>Load user by email</li>
     *   <li>Check account status (active, not locked, not deleted)</li>
     *   <li>Validate password — increment failure counter and potentially lock on mismatch</li>
     *   <li>Reset failure counter on success</li>
     *   <li>Issue refresh token and return AuthResponse</li>
     * </ol>
     *
     * @param request    the validated login payload
     * @param deviceInfo the User-Agent string
     * @param ipAddress  the client IP
     * @return {@link AuthResult} containing the access token, user profile, and raw refresh token
     */
    public AuthResult login(LoginRequest request, String deviceInfo, String ipAddress) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException(
                        "INVALID_CREDENTIALS", "Email or password is incorrect."));

        // Check account status
        validateAccountStatus(user);

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Email or password is incorrect.");
        }

        // Successful login — reset failure counter
        userRepository.resetLoginFailures(user.getId());

        // Issue refresh token — raw token returned to controller to set as HttpOnly cookie
        String rawRefreshToken = refreshTokenService.issueToken(
                user.getId(), deviceInfo, ipAddress, request.isRememberMe());

        log.info("User {} logged in successfully", user.getEmail());
        return new AuthResult(jwtService.buildAuthResponse(user, toUserResponse(user)), rawRefreshToken);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFRESH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Issues a new access token using a valid refresh token cookie.
     *
     * <p>The old refresh token is rotated (revoked and replaced) on every call.
     * The new raw refresh token is returned for the controller to set as a cookie.
     *
     * @param rawRefreshToken the raw refresh token from the HttpOnly cookie
     * @return {@link AuthResult} containing the new access token and the new raw refresh token
     */
    public AuthResult refresh(String rawRefreshToken) {
        // Validate and rotate the refresh token
        String newRawToken = refreshTokenService.rotateToken(rawRefreshToken);

        // Get the user ID from the newly issued replacement token
        UUID userId = refreshTokenService.getUserIdFromToken(newRawToken);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException(
                        "USER_NOT_FOUND", "Associated account not found."));

        validateAccountStatus(user);

        log.debug("Token refreshed for user {}", user.getEmail());
        return new AuthResult(jwtService.buildAuthResponse(user, toUserResponse(user)), newRawToken);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Logs out the current session by revoking the refresh token.
     *
     * @param rawRefreshToken the raw refresh token from the cookie (may be null if cookie missing)
     */
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeToken(rawRefreshToken);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Changes the password for an authenticated user who knows their current password.
     *
     * <p>After a successful change, all refresh tokens are revoked (forces re-login
     * from all devices), and a security notification email is sent.
     *
     * @param userId  the authenticated user's UUID
     * @param request the change password payload
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_CURRENT_PASSWORD", "Current password is incorrect.");
        }

        // Prevent setting the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("PASSWORD_SAME_AS_CURRENT",
                    "New password must be different from the current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all sessions — user must re-login on all devices
        refreshTokenService.revokeAllTokensForUser(userId);

        // Security notification (async)
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());
        log.info("Password changed for user {}", userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET CURRENT USER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current authenticated user's profile.
     * Called by {@code GET /api/v1/users/me}.
     *
     * @param userId the authenticated user's UUID
     * @return the user profile DTO
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));
        return toUserResponse(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT DELETION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes the authenticated user's account.
     *
     * <p>Sets {@code deleted_at} and {@code status = DELETED}. All refresh tokens are revoked.
     * Data is retained per GDPR right-to-erasure policy (full deletion via separate job).
     *
     * @param userId          the user's UUID
     * @param currentPassword the user's current password for final confirmation
     */
    public void deleteAccount(UUID userId, String currentPassword) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found."));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_PASSWORD", "Password confirmation failed.");
        }

        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        refreshTokenService.revokeAllTokensForUser(userId);
        log.info("Account soft-deleted for user {}", userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates that the user's account is in a state that allows login.
     *
     * @param user the user to validate
     * @throws UnauthorizedException if the account is suspended, deleted, or locked
     */
    private void validateAccountStatus(User user) {
        if (user.isDeleted() || user.getStatus() == UserStatus.DELETED) {
            throw new UnauthorizedException("ACCOUNT_DELETED", "This account no longer exists.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("ACCOUNT_SUSPENDED",
                    "This account has been suspended. Please contact support.");
        }
        if (user.isAccountLocked()) {
            throw new UnauthorizedException("ACCOUNT_LOCKED",
                    "Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }
    }

    /**
     * Records a failed login attempt and locks the account if the threshold is reached.
     *
     * @param user the user who failed to authenticate
     */
    private void handleFailedLogin(User user) {
        userRepository.incrementFailedLoginCount(user.getId());
        int newCount = user.getFailedLoginCount() + 1;

        if (newCount >= maxLoginAttempts) {
            Instant lockUntil = Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
            userRepository.lockAccount(user.getId(), lockUntil);
            log.warn("Account {} locked until {} after {} failed attempts",
                    user.getEmail(), lockUntil, newCount);
        } else {
            log.debug("Failed login attempt {} of {} for {}", newCount, maxLoginAttempts, user.getEmail());
        }
    }

    /**
     * Generates a unique handle by appending numeric suffixes until no conflict is found.
     *
     * @param baseHandle the handle derived from the user's display name
     * @return a unique handle string
     */
    private String generateUniqueHandle(String baseHandle) {
        if (!userRepository.existsByHandle(baseHandle)) {
            return baseHandle;
        }
        int suffix = 2;
        while (true) {
            String candidate = SlugUtils.withSuffix(baseHandle, suffix);
            if (!userRepository.existsByHandle(candidate)) {
                return candidate;
            }
            suffix++;
        }
    }

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO.
     * {@code creatorProfileId} will be populated in Phase 2 (Creator module).
     *
     * @param user the user entity to map
     * @return the public-facing user DTO
     */
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .handle(user.getHandle())
                .role(user.getRole().name().toLowerCase())
                .avatarUrl(user.getAvatarUrl())
                .coverUrl(user.getCoverUrl())
                .bio(user.getBio())
                .city(user.getCity())
                .website(user.getWebsite())
                .isVerified(user.isVerified())
                .emailVerified(user.isEmailVerified())
                .creatorProfileId(null) // Populated in Phase 2
                .build();
    }
}
