package com.drawpin.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/verify-email}.
 *
 * <p>The raw token is extracted from the verification email link's query parameter
 * and submitted here to mark the user's email as verified.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#verifyEmail}</li>
 *   <li>{@link com.drawpin.service.auth.EmailVerificationService#verify}</li>
 * </ul>
 */
@Data
@Schema(description = "Token from the email verification link")
public class VerifyEmailRequest {

    @NotBlank(message = "Verification token is required")
    @Schema(description = "Raw token extracted from the verification email link")
    private String token;
}
