package com.drawpin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing one active device session.
 *
 * <p>Returned as a list by {@code GET /api/v1/auth/sessions}.
 * The frontend's "Active Sessions" settings page renders one card per session,
 * showing device info and allowing the user to revoke individual sessions.
 *
 * <p><b>Note:</b> The raw token or its hash is never included in this response.
 * The session UUID is used to identify the session for revocation.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.SessionController#getSessions}</li>
 *   <li>{@link com.drawpin.controller.auth.SessionController#revokeSession}</li>
 *   <li>{@link com.drawpin.service.auth.RefreshTokenService#getActiveSessions}</li>
 * </ul>
 */
@Data
@Builder
@Schema(description = "An active login session — one per device")
public class SessionResponse {

    @Schema(description = "Session UUID used to revoke this session")
    private UUID sessionId;

    @Schema(description = "Browser and OS information", example = "Chrome on Windows 11")
    private String deviceInfo;

    @Schema(description = "IP address at the time of login", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "When this session was created (login time)")
    private Instant createdAt;

    @Schema(description = "When this session will expire")
    private Instant expiresAt;

    @Schema(description = "True if this is the current session making the request", example = "true")
    private boolean current;
}
