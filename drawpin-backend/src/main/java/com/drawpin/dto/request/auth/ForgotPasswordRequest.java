package com.drawpin.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/forgot-password}.
 *
 * <p>The server always responds with 200 regardless of whether the email exists,
 * preventing email enumeration attacks.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#forgotPassword}</li>
 *   <li>{@link com.drawpin.service.auth.PasswordResetService#initiateReset}</li>
 * </ul>
 */
@Data
@Schema(description = "Email address to send a password reset link to")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "The email address associated with your account", example = "aria@example.com")
    private String email;
}
