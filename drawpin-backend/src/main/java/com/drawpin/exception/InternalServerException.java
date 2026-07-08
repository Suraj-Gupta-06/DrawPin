package com.drawpin.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unexpected server error occurs (e.g. failing to communicate with Cloudinary).
 * Maps to HTTP {@code 500 Internal Server Error}.
 */
public class InternalServerException extends DrawPinException {

    public InternalServerException(String errorCode, String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorCode, message);
    }
}
