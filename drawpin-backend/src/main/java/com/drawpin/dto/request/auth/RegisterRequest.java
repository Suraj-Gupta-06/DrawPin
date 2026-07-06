package com.drawpin.dto.request.auth;

import com.drawpin.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/register}.
 *
 * <p>Validated by Spring's {@code @Valid} before the request reaches
 * {@link com.drawpin.service.auth.AuthService#register(RegisterRequest, String)}.
 * All validation errors are captured and returned as a structured
 * {@link com.drawpin.dto.response.ErrorResponse} by the global exception handler.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.controller.auth.AuthController#register}</li>
 *   <li>{@link com.drawpin.service.auth.AuthService#register}</li>
 * </ul>
 */
@Data
@Schema(description = "Payload for creating a new DrawPin account")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    @Schema(description = "Full display name", example = "Aria Vance")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Email address used for login", example = "aria@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    @Schema(description = "Password (min 8 chars, must include uppercase, lowercase, and digit)", example = "Str0ng!Pass")
    private String password;

    /**
     * Optional role selection. Defaults to {@code COLLECTOR} if not provided.
     * Only {@code COLLECTOR} and {@code CREATOR} are accepted from the public registration endpoint.
     * Admin and Moderator roles are assigned by admins only.
     */
    @Schema(description = "Initial role. Defaults to COLLECTOR.", example = "creator",
            allowableValues = {"collector", "creator"})
    private UserRole role;
}
