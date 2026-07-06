package com.drawpin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Structured error payload nested inside {@link ApiResponse} on failure.
 *
 * <p>Every exception thrown in the service layer is caught by
 * {@link com.drawpin.exception.GlobalExceptionHandler} and mapped to an
 * {@code ErrorResponse} with a machine-readable {@code code} and a
 * human-readable {@code message}.
 *
 * <p>The {@code fields} map is populated only for {@code 400 Validation} errors,
 * mapping each invalid field name to its specific error message so the
 * frontend can display field-level errors inline.
 *
 * <p><b>Example error response:</b>
 * <pre>{@code
 * {
 *   "success": false,
 *   "error": {
 *     "code": "VALIDATION_FAILED",
 *     "message": "Request validation failed",
 *     "fields": {
 *       "email": "Must be a valid email address",
 *       "password": "Password must be between 8 and 128 characters"
 *     }
 *   }
 * }
 * }</pre>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Structured error details returned on any API failure")
public class ErrorResponse {

    /** Machine-readable error code, e.g. {@code EMAIL_ALREADY_EXISTS}, {@code TOKEN_EXPIRED}. */
    @Schema(description = "Machine-readable error code", example = "EMAIL_ALREADY_EXISTS")
    private String code;

    /** Human-readable error message safe to display in the UI. */
    @Schema(description = "Human-readable error description", example = "An account with this email already exists.")
    private String message;

    /**
     * Field-level validation errors. Present only for {@code 400 VALIDATION_FAILED} errors.
     * Key = field name, Value = error message for that field.
     */
    @Schema(description = "Field-level validation errors — present only for 400 responses")
    private Map<String, String> fields;

    // ─────────────────────────────────────────────────────────────────────────
    // FACTORY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /** Creates an {@code ErrorResponse} with a code and message only. */
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder().code(code).message(message).build();
    }

    /** Creates a validation error response with field-level details. */
    public static ErrorResponse validation(Map<String, String> fields) {
        return ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("Request validation failed")
                .fields(fields)
                .build();
    }
}
