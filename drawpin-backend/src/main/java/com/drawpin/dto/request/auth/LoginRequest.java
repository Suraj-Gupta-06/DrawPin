package com.drawpin.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#login}</li>
 *   <li>{@link com.drawpin.service.auth.AuthService#login}</li>
 * </ul>
 */
@Data
@Schema(description = "Credentials for authenticating an existing user")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Registered email address", example = "aria@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Str0ng!Pass")
    private String password;

    /**
     * When {@code true}, the refresh token's expiry is extended to 30 days (instead of 14).
     * Mapped to the "Remember me" checkbox on the login page.
     */
    @Schema(description = "Extend session to 30 days when true", example = "false")
    private boolean rememberMe;
}
