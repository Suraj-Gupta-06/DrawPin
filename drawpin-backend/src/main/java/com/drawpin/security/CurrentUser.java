package com.drawpin.security;

import java.lang.annotation.*;

/**
 * Method parameter annotation that injects the currently authenticated
 * {@link DrawPinUserDetails} into a controller method argument.
 *
 * <p>Works in conjunction with Spring Security's {@code @AuthenticationPrincipal}
 * meta-annotation. Using this custom annotation keeps controllers clean — no need to
 * write {@code @AuthenticationPrincipal DrawPinUserDetails principal} on every method.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * @GetMapping("/me")
 * public ResponseEntity<ApiResponse<UserResponse>> getMe(@CurrentUser DrawPinUserDetails principal) {
 *     // principal is automatically injected from SecurityContextHolder
 * }
 * }</pre>
 *
 * <p>Returns {@code null} if the endpoint is called without authentication.
 * Always use on endpoints secured by Spring Security to avoid null checks.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.security.core.annotation.AuthenticationPrincipal
public @interface CurrentUser {
}
