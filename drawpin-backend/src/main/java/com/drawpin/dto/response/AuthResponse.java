package com.drawpin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO returned by {@code POST /api/v1/auth/login} and
 * {@code POST /api/v1/auth/register}.
 *
 * <p>Contains the short-lived JWT access token and the embedded user profile.
 * The refresh token is NOT returned in the body — it is set as an HttpOnly
 * cookie by the controller to prevent JavaScript access.
 *
 * <p><b>Frontend consumption:</b>
 * The {@code useAuth()} hook stores {@code accessToken} in memory (not localStorage)
 * and stores {@code user} in the auth context. On every API call the Axios interceptor
 * adds the access token to the {@code Authorization: Bearer} header.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#login}</li>
 *   <li>{@link com.drawpin.controller.auth.AuthController#register}</li>
 *   <li>{@link com.drawpin.controller.auth.AuthController#refresh}</li>
 *   <li>{@link com.drawpin.service.auth.AuthService}</li>
 * </ul>
 */
@Data
@Builder
@Schema(description = "Authentication response containing the access token and user profile")
public class AuthResponse {

    /**
     * Short-lived JWT access token (15 minutes).
     * Must be sent as {@code Authorization: Bearer <accessToken>} on every protected request.
     */
    @Schema(description = "JWT access token — valid for 15 minutes")
    private String accessToken;

    /** Always {@code "Bearer"} — informs the frontend how to use the token. */
    @Schema(description = "Token type — always Bearer", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token TTL in seconds. Allows the frontend to schedule a proactive
     * refresh before the token actually expires.
     */
    @Schema(description = "Access token lifetime in seconds", example = "900")
    private long expiresIn;

    /** The authenticated user's public profile. */
    @Schema(description = "The authenticated user's profile data")
    private UserResponse user;
}
