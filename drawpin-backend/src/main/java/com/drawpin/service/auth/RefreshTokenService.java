package com.drawpin.service.auth;

import com.drawpin.config.JwtConfig;
import com.drawpin.domain.entity.RefreshToken;
import com.drawpin.dto.response.SessionResponse;
import com.drawpin.exception.UnauthorizedException;
import com.drawpin.repository.RefreshTokenRepository;
import com.drawpin.util.HashUtils;
import com.drawpin.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manages the full lifecycle of refresh tokens.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Issue new refresh tokens on login</li>
 *   <li>Rotate (replace) refresh tokens on every /refresh call</li>
 *   <li>Revoke individual tokens on logout</li>
 *   <li>Revoke all tokens for a user on password change or account deletion</li>
 *   <li>Return active session list for the sessions page</li>
 * </ul>
 *
 * <p><b>Security design:</b>
 * <ul>
 *   <li>Raw tokens are never stored — only their SHA-256 hash is persisted</li>
 *   <li>Token rotation on every refresh prevents token replay attacks</li>
 *   <li>Revoked tokens are marked but not deleted immediately (for audit)</li>
 * </ul>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link RefreshTokenRepository} — DB persistence</li>
 *   <li>{@link AuthService} — calls issue/rotate/revoke</li>
 *   <li>{@link com.drawpin.controller.auth.SessionController} — calls getActiveSessions</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    /**
     * Issues a new refresh token for a user's login session.
     *
     * <p>The raw token is returned once and must be immediately set as an HttpOnly cookie.
     * Only the SHA-256 hash is stored in the database.
     *
     * @param userId      the user's UUID
     * @param deviceInfo  browser/OS string from the User-Agent header
     * @param ipAddress   IP address of the client
     * @param rememberMe  if {@code true}, uses the extended 30-day expiry
     * @return the raw refresh token string (NOT the hash)
     */
    public String issueToken(UUID userId, String deviceInfo, String ipAddress, boolean rememberMe) {
        String rawToken = TokenUtils.generate();
        String tokenHash = HashUtils.sha256(rawToken);

        long expirySeconds = rememberMe
                ? jwtConfig.getRememberMeExpirySeconds()
                : jwtConfig.getRefreshTokenExpirySeconds();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(expirySeconds))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Issued refresh token for user {} (deviceInfo: {})", userId, deviceInfo);
        return rawToken;
    }

    /**
     * Validates and rotates a refresh token.
     *
     * <p>Token rotation is the core of the refresh flow:
     * <ol>
     *   <li>Hash the submitted raw token</li>
     *   <li>Look it up in the database</li>
     *   <li>Validate it (not revoked, not expired)</li>
     *   <li>Revoke the old token</li>
     *   <li>Issue a new token with the same metadata</li>
     * </ol>
     *
     * @param rawToken the raw token from the HttpOnly cookie
     * @return the new raw refresh token to replace the old cookie
     * @throws UnauthorizedException if the token is invalid, revoked, or expired
     */
    public String rotateToken(String rawToken) {
        String tokenHash = HashUtils.sha256(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(
                        "INVALID_REFRESH_TOKEN", "Refresh token not found. Please log in again."));

        if (!existing.isValid()) {
            // Revoked or expired token — revoke all tokens for this user (potential theft detection)
            log.warn("Attempted use of invalid/expired/revoked refresh token for user {}. Revoking all sessions.",
                    existing.getUserId());
            refreshTokenRepository.revokeAllByUserId(existing.getUserId());
            throw new UnauthorizedException(
                    "REFRESH_TOKEN_INVALID", "Session is no longer valid. Please log in again.");
        }

        // Revoke the consumed token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Issue a replacement token with the same metadata
        return issueToken(
                existing.getUserId(),
                existing.getDeviceInfo(),
                existing.getIpAddress(),
                false // Keep standard expiry on rotation (no remember-me extension)
        );
    }

    /**
     * Revokes a specific refresh token by its raw value (used during logout).
     *
     * @param rawToken the raw token from the cookie
     */
    public void revokeToken(String rawToken) {
        String tokenHash = HashUtils.sha256(rawToken);
        refreshTokenRepository.revokeByTokenHash(tokenHash);
        log.debug("Refresh token revoked via logout");
    }

    /**
     * Revokes all refresh tokens for a user.
     * Called on password change, password reset, and account deletion.
     *
     * @param userId the user's UUID
     */
    public void revokeAllTokensForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All refresh tokens revoked for user {}", userId);
    }

    /**
     * Returns the list of active sessions for the sessions management page.
     *
     * @param userId         the current user's UUID
     * @param currentTokenHash the SHA-256 hash of the current session's refresh token
     *                         (used to mark the calling session as "current")
     * @return list of {@link SessionResponse} DTOs — one per active device
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(UUID userId, String currentTokenHash) {
        return refreshTokenRepository.findValidSessionsByUserId(userId, Instant.now())
                .stream()
                .map(token -> SessionResponse.builder()
                        .sessionId(token.getId())
                        .deviceInfo(token.getDeviceInfo())
                        .ipAddress(token.getIpAddress())
                        .createdAt(token.getCreatedAt())
                        .expiresAt(token.getExpiresAt())
                        .current(currentTokenHash != null && currentTokenHash.equals(token.getTokenHash()))
                        .build())
                .toList();
    }

    /**
     * Revokes a specific session by its UUID.
     * Validates that the session belongs to the requesting user before revoking.
     *
     * @param userId    the current user's UUID
     * @param sessionId the UUID of the session to revoke
     * @throws UnauthorizedException if the session does not belong to the user
     */
    public void revokeSession(UUID userId, UUID sessionId) {
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new UnauthorizedException("SESSION_NOT_FOUND", "Session not found."));

        if (!token.getUserId().equals(userId)) {
            throw new UnauthorizedException("SESSION_NOT_OWNED", "This session does not belong to you.");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.debug("Session {} revoked for user {}", sessionId, userId);
    }

    /**
     * Extracts the user UUID from a valid raw refresh token without rotating it.
     * Used during the refresh flow to identify the user before issuing a new access token.
     *
     * @param rawToken the raw token from the cookie
     * @return the user UUID
     * @throws UnauthorizedException if the token is invalid
     */
    @Transactional(readOnly = true)
    public UUID getUserIdFromToken(String rawToken) {
        String tokenHash = HashUtils.sha256(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(
                        "INVALID_REFRESH_TOKEN", "Refresh token not found."));
        if (!token.isValid()) {
            throw new UnauthorizedException("REFRESH_TOKEN_EXPIRED", "Session expired. Please log in again.");
        }
        return token.getUserId();
    }
}
