package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user attempts an action they are not authorised to perform.
 * Maps to HTTP {@code 403 Forbidden}.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * throw new ForbiddenException("INSUFFICIENT_ROLE", "Only creators can publish services.");
 * throw new ForbiddenException("NOT_RESOURCE_OWNER", "You can only edit your own content.");
 * }</pre>
 */
public class ForbiddenException extends DrawPinException {

    public ForbiddenException(String errorCode, String message) {
        super(HttpStatus.FORBIDDEN, errorCode, message);
    }
}
