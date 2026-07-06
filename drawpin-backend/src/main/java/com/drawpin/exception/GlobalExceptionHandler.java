package com.drawpin.exception;

import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler — the single catch-all for all exceptions thrown across
 * the entire controller layer.
 *
 * <p>Every exception is converted into a standardised
 * {@link ApiResponse}{@code <ErrorResponse>} so the frontend always receives
 * a consistent shape regardless of the error type.
 *
 * <p><b>Handled exception types:</b>
 * <ul>
 *   <li>{@link DrawPinException} subtypes — domain errors with typed HTTP status</li>
 *   <li>{@link MethodArgumentNotValidException} — Bean Validation (@Valid) failures → 400</li>
 *   <li>Spring Security exceptions (BadCredentials, Disabled, Locked, AccessDenied) → 401/403</li>
 *   <li>{@link Exception} fallback → 500 with a generic message (no stack trace in response)</li>
 * </ul>
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>All controller classes — any uncaught exception bubbles here</li>
 *   <li>{@link ApiResponse} — all responses wrapped in this envelope</li>
 *   <li>{@link ErrorResponse} — the error payload inside the envelope</li>
 * </ul>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // DRAWPIN DOMAIN EXCEPTIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles all {@link DrawPinException} subtypes.
     * Each subclass carries its own {@link HttpStatus} and error code.
     *
     * @param ex      the domain exception
     * @param request the current HTTP request (for logging)
     * @return structured error response with the exception's status code
     */
    @ExceptionHandler(DrawPinException.class)
    public ResponseEntity<ApiResponse<Void>> handleDrawPinException(
            DrawPinException ex, HttpServletRequest request) {
        log.warn("[{}] {} {} — {}: {}",
                ex.getStatus().value(), request.getMethod(), request.getRequestURI(),
                ex.getErrorCode(), ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.fail(ErrorResponse.of(ex.getErrorCode(), ex.getMessage())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BEAN VALIDATION FAILURES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles {@code @Valid} validation failures on request bodies.
     * Collects all field-level errors into a map and returns a 400 with per-field messages.
     *
     * @param ex      the validation exception containing all field errors
     * @param request the current HTTP request
     * @return 400 response with a field → message map in the error payload
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing  // keep first error per field
                ));

        log.debug("[400] {} {} — validation failed on fields: {}",
                request.getMethod(), request.getRequestURI(), fieldErrors.keySet());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorResponse.validation(fieldErrors)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SPRING SECURITY EXCEPTIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles Spring Security's {@link BadCredentialsException} — wrong password.
     *
     * @param ex the exception
     * @return 401 INVALID_CREDENTIALS
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorResponse.of(
                        "INVALID_CREDENTIALS", "Email or password is incorrect.")));
    }

    /**
     * Handles Spring Security's {@link DisabledException} — account is suspended or deleted.
     *
     * @param ex the exception
     * @return 401 ACCOUNT_DISABLED
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorResponse.of(
                        "ACCOUNT_DISABLED", "This account has been suspended or deleted.")));
    }

    /**
     * Handles Spring Security's {@link LockedException} — too many failed login attempts.
     *
     * @param ex the exception
     * @return 401 ACCOUNT_LOCKED
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocked(LockedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorResponse.of(
                        "ACCOUNT_LOCKED", "Account is temporarily locked. Please try again later.")));
    }

    /**
     * Handles Spring Security's {@link AccessDeniedException} — authenticated but not authorised.
     *
     * @param ex the exception
     * @return 403 ACCESS_DENIED
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorResponse.of(
                        "ACCESS_DENIED", "You do not have permission to perform this action.")));
    }

    /**
     * Handles any other Spring Security {@link AuthenticationException}.
     *
     * @param ex the exception
     * @return 401 AUTHENTICATION_FAILED
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorResponse.of(
                        "AUTHENTICATION_FAILED", "Authentication failed.")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fallback handler for any uncaught exception.
     * Logs the full stack trace server-side but returns only a safe generic message
     * to prevent information leakage in production.
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(ErrorResponse.of(
                        "INTERNAL_ERROR", "An unexpected error occurred. Please try again later.")));
    }
}
