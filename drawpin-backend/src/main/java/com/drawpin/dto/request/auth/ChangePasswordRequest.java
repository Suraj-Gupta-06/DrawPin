package com.drawpin.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/change-password}.
 *
 * <p>This endpoint is for authenticated users who know their current password.
 * After a successful change, all other refresh token sessions are revoked.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#changePassword}</li>
 *   <li>{@link com.drawpin.service.auth.AuthService#changePassword}</li>
 * </ul>
 */
@Data
@Schema(description = "Current password and the new desired password")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "The user's existing password for verification", example = "OldStr0ng!Pass")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(description = "The new password to set", example = "NewStr0ng!Pass")
    private String newPassword;
}
