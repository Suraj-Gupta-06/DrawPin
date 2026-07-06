package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a resource creation or update conflicts with existing data.
 * Maps to HTTP {@code 409 Conflict}.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new ConflictException("EMAIL_ALREADY_EXISTS", "An account with this email already exists.");
 * throw new ConflictException("HANDLE_ALREADY_EXISTS", "This handle is already taken.");
 * }</pre>
 */
public class ConflictException extends DrawPinException {

    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
