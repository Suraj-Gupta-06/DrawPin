package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a client exceeds a configured rate limit.
 * Maps to HTTP {@code 429 Too Many Requests}.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new RateLimitException("RESET_EMAIL_RATE_LIMIT",
 *     "A password reset email was already sent. Please wait 5 minutes before requesting another.");
 * throw new RateLimitException("LOGIN_RATE_LIMIT",
 *     "Too many login attempts. Please try again in 15 minutes.");
 * }</pre>
 */
public class RateLimitException extends DrawPinException {

    public RateLimitException(String errorCode, String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, errorCode, message);
    }
}
