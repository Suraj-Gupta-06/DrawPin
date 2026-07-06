package com.drawpin.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/reset-password}.
 *
 * <p>The raw token from the password reset email link is included in this payload
 * alongside the new password. The server hashes the token and validates it against
 * the database before updating the password.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#resetPassword}</li>
 *   <li>{@link com.drawpin.service.auth.PasswordResetService#resetPassword}</li>
 * </ul>
 */
@Data
@Schema(description = "Token from the reset email plus the new desired password")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Raw token from the password reset email link")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(description = "The new password to set", example = "NewStr0ng!Pass")
    private String newPassword;
}
