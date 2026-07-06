package com.drawpin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response DTO for the authenticated user's public-facing profile fields.
 *
 * <p>Embedded inside {@link AuthResponse} on login/register and returned
 * standalone by {@code GET /api/v1/users/me}.
 *
 * <p>This DTO mirrors the shape expected by the DrawPin React frontend's
 * auth context ({@code useAuth()} hook). Every field name must match exactly
 * what the frontend destructures.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link AuthResponse} — embedded as the {@code user} field</li>
 *   <li>{@code GET /api/v1/users/me} — returned as the top-level data payload</li>
 * </ul>
 */
@Data
@Builder
@Schema(description = "Public-facing user profile fields returned after authentication")
public class UserResponse {

    @Schema(description = "User's UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Display name", example = "Aria Vance")
    private String name;

    @Schema(description = "Email address", example = "aria@example.com")
    private String email;

    @Schema(description = "Unique handle / username", example = "aria.vance")
    private String handle;

    @Schema(description = "Platform role", example = "creator")
    private String role;

    @Schema(description = "Cloudinary avatar URL — null until uploaded")
    private String avatarUrl;

    @Schema(description = "Cloudinary cover URL — null until uploaded")
    private String coverUrl;

    @Schema(description = "Short bio text")
    private String bio;

    @Schema(description = "City or location string", example = "Berlin, Germany")
    private String city;

    @Schema(description = "Personal website URL", example = "aria.studio")
    private String website;

    @Schema(description = "Whether the user has the verified badge", example = "false")
    private boolean isVerified;

    @Schema(description = "Whether the user has confirmed their email", example = "false")
    private boolean emailVerified;

    /**
     * UUID of the creator profile, if the user has one.
     * {@code null} for collector-role users.
     * Used by the frontend to determine whether to show creator-specific UI.
     */
    @Schema(description = "Creator profile UUID — null for non-creator accounts")
    private UUID creatorProfileId;
}
