package com.drawpin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Universal API response envelope.
 *
 * <p>Every endpoint in DrawPin — success or error — returns this wrapper.
 * The frontend {@code Axios} interceptor reads the {@code success} flag first,
 * then unwraps {@code data} on success or reads {@code error} on failure.
 *
 * <p><b>Success shape:</b>
 * <pre>{@code
 * {
 *   "success": true,
 *   "timestamp": "2026-07-04T10:00:00Z",
 *   "data": { ... }
 * }
 * }</pre>
 *
 * <p><b>Error shape (handled by GlobalExceptionHandler):</b>
 * <pre>{@code
 * {
 *   "success": false,
 *   "timestamp": "2026-07-04T10:00:00Z",
 *   "error": { "code": "EMAIL_ALREADY_EXISTS", "message": "...", "fields": {} }
 * }
 * }</pre>
 *
 * @param <T> the type of the {@code data} payload
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope used by every DrawPin endpoint")
public class ApiResponse<T> {

    /** Whether the operation completed successfully. */
    @Schema(description = "True if the request succeeded", example = "true")
    private boolean success;

    /** Server-side timestamp of when the response was generated. */
    @Schema(description = "UTC timestamp of the response")
    @Builder.Default
    private Instant timestamp = Instant.now();

    /** The response payload. Present only on success. */
    @Schema(description = "The response data payload — absent on error")
    private T data;

    /** The error details. Present only on failure. */
    @Schema(description = "Error details — absent on success")
    private ErrorResponse error;

    // ─────────────────────────────────────────────────────────────────────────
    // FACTORY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a successful response wrapping the given data payload.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a successful {@code ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with a simple message string as the payload.
     *
     * @param message the success message
     * @return a successful {@code ApiResponse} with a message-only payload
     */
    public static ApiResponse<MessagePayload> ok(String message) {
        return ApiResponse.<MessagePayload>builder()
                .success(true)
                .data(new MessagePayload(message))
                .build();
    }

    /**
     * Creates a failed response wrapping an {@link ErrorResponse}.
     *
     * @param error the structured error details
     * @param <T>   phantom type for the absent data
     * @return a failed {@code ApiResponse}
     */
    public static <T> ApiResponse<T> fail(ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    /** Simple payload record for message-only successful responses. */
    public record MessagePayload(String message) {}
}
