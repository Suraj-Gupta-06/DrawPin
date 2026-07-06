package com.drawpin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all DrawPin domain exceptions.
 *
 * <p>Every application-level exception extends this class. The {@link GlobalExceptionHandler}
 * catches all {@code DrawPinException} subtypes and maps them to the appropriate HTTP status
 * code and a structured {@link com.drawpin.dto.response.ErrorResponse}.
 *
 * <p><b>Subclasses define specific error scenarios:</b>
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404</li>
 *   <li>{@link UnauthorizedException} → 401</li>
 *   <li>{@link ForbiddenException} → 403</li>
 *   <li>{@link ConflictException} → 409</li>
 *   <li>{@link ValidationException} → 400</li>
 *   <li>{@link RateLimitException} → 429</li>
 * </ul>
 */
@Getter
public abstract class DrawPinException extends RuntimeException {

    /** HTTP status code to return for this exception type. */
    private final HttpStatus status;

    /**
     * Machine-readable error code embedded in the {@link com.drawpin.dto.response.ErrorResponse}.
     * Examples: {@code EMAIL_ALREADY_EXISTS}, {@code TOKEN_EXPIRED}.
     */
    private final String errorCode;

    /**
     * Constructs a base DrawPin exception.
     *
     * @param status    the HTTP status to respond with
     * @param errorCode machine-readable error identifier
     * @param message   human-readable message safe to expose in the UI
     */
    protected DrawPinException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Constructs a base DrawPin exception with a root cause.
     *
     * @param status    the HTTP status to respond with
     * @param errorCode machine-readable error identifier
     * @param message   human-readable message
     * @param cause     the underlying cause
     */
    protected DrawPinException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
