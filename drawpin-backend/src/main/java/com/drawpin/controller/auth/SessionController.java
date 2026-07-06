package com.drawpin.controller.auth;

import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.SessionResponse;
import com.drawpin.security.CurrentUser;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.auth.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.drawpin.util.HashUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing active login sessions.
 *
 * <p>Base path: {@code /auth/sessions}<br>
 * Full path (with context): {@code /api/v1/auth/sessions}
 *
 * <p>All endpoints require authentication.
 *
 * <p><b>Endpoints:</b>
 * <ul>
 *   <li>{@code GET /auth/sessions} — list all active sessions</li>
 *   <li>{@code DELETE /auth/sessions/{sessionId}} — revoke a specific session</li>
 *   <li>{@code DELETE /auth/sessions} — revoke all other sessions (keep current)</li>
 * </ul>
 *
 * <p><b>Frontend consumption:</b> The "Active Sessions" section on the security settings page.
 */
@RestController
@RequestMapping("/auth/sessions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sessions", description = "Manage active login sessions across devices")
public class SessionController {

    private static final String REFRESH_TOKEN_COOKIE = "drawpin_refresh_token";

    private final RefreshTokenService refreshTokenService;

    // ─────────────────────────────────────────────────────────────────────────
    // LIST SESSIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a list of all active sessions for the current user.
     * The current session is marked with {@code "current": true}.
     *
     * @param principal the authenticated user
     * @param httpReq   to extract the current session's refresh cookie
     * @return list of active sessions
     */
    @GetMapping
    @Operation(summary = "List active sessions",
               description = "Returns all active login sessions. The calling session is marked as 'current'.")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSessions(
            @CurrentUser DrawPinUserDetails principal,
            HttpServletRequest httpReq) {

        String rawRefreshToken = extractRefreshTokenCookie(httpReq);
        String currentTokenHash = rawRefreshToken != null ? HashUtils.sha256(rawRefreshToken) : null;

        List<SessionResponse> sessions = refreshTokenService.getActiveSessions(
                principal.getUserId(), currentTokenHash);

        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REVOKE SESSION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Revokes a specific session by its UUID.
     * Can be used to remotely log out from another device.
     *
     * @param sessionId the UUID of the session to revoke
     * @param principal the authenticated user (ownership is validated in the service)
     * @return 200 on success
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Revoke a session",
               description = "Revokes a specific active session. Can revoke sessions from other devices.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> revokeSession(
            @PathVariable UUID sessionId,
            @CurrentUser DrawPinUserDetails principal) {

        refreshTokenService.revokeSession(principal.getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session revoked successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REVOKE ALL OTHER SESSIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Revokes all sessions for the user (logs out from all devices).
     * This does NOT revoke the current session — that requires an explicit logout.
     *
     * @param principal the authenticated user
     * @return 200 on success
     */
    @DeleteMapping
    @Operation(summary = "Revoke all sessions",
               description = "Logs out from all devices by revoking all refresh tokens.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> revokeAllSessions(
            @CurrentUser DrawPinUserDetails principal) {

        refreshTokenService.revokeAllTokensForUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("All sessions have been revoked."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts the refresh token from the request cookies.
     *
     * @param request the HTTP request
     * @return the raw refresh token, or {@code null} if not present
     */
    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
