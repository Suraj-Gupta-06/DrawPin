package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business-logic validation fails that cannot be expressed by Bean Validation.
 * Maps to HTTP {@code 400 Bad Request}.
 *
 * <p>This is distinct from field-level {@code @Valid} violations handled by the
 * {@code MethodArgumentNotValidException} path in the exception handler.
 * Use this for semantic validation failures (e.g., "new password cannot be the same
 * as the old password").
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new ValidationException("PASSWORD_SAME_AS_CURRENT", "New password must differ from the current password.");
 * throw new ValidationException("INVALID_TOKEN", "This reset link has already been used or has expired.");
 * }</pre>
 */
public class ValidationException extends DrawPinException {

    public ValidationException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
