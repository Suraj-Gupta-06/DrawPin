package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 * Maps to HTTP {@code 404 Not Found}.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new ResourceNotFoundException("USER_NOT_FOUND", "No user found with ID: " + id);
 * throw new ResourceNotFoundException("PIN_NOT_FOUND", "Pin not found: " + pinId);
 * }</pre>
 */
public class ResourceNotFoundException extends DrawPinException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}
