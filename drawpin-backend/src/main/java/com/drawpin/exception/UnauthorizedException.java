package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication is required but the request is unauthenticated,
 * or when provided credentials are invalid.
 * Maps to HTTP {@code 401 Unauthorized}.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new UnauthorizedException("INVALID_CREDENTIALS", "Email or password is incorrect.");
 * throw new UnauthorizedException("TOKEN_EXPIRED", "Your session has expired. Please log in again.");
 * throw new UnauthorizedException("ACCOUNT_LOCKED", "Account is temporarily locked due to failed login attempts.");
 * }</pre>
 */
public class UnauthorizedException extends DrawPinException {

    public UnauthorizedException(String errorCode, String message) {
        super(HttpStatus.UNAUTHORIZED, errorCode, message);
    }
}
