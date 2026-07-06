package com.drawpin.controller.auth;

import com.drawpin.dto.request.auth.*;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.AuthResponse;
import com.drawpin.dto.response.AuthResult;
import com.drawpin.dto.response.UserResponse;
import com.drawpin.security.CurrentUser;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.auth.AuthService;
import com.drawpin.service.auth.EmailVerificationService;
import com.drawpin.service.auth.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * REST controller for all authentication operations.
 *
 * <p>Base path: {@code /auth}<br>
 * Full path (with context): {@code /api/v1/auth}
 *
 * <p>All endpoints return the standard {@link ApiResponse} envelope.
 * The refresh token is managed exclusively through HttpOnly cookies — it is
 * never returned in the response body to prevent JavaScript XSS access.
 *
 * <p><b>Endpoints:</b>
 * <ul>
 *   <li>{@code POST /auth/register} — create account</li>
 *   <li>{@code POST /auth/login} — authenticate</li>
 *   <li>{@code POST /auth/refresh} — rotate access token</li>
 *   <li>{@code POST /auth/logout} — revoke session</li>
 *   <li>{@code POST /auth/forgot-password} — initiate password reset</li>
 *   <li>{@code POST /auth/reset-password} — apply new password from reset link</li>
 *   <li>{@code POST /auth/verify-email} — confirm email address</li>
 *   <li>{@code POST /auth/change-password} — change password (authenticated)</li>
 *   <li>{@code GET /auth/me} — get current user profile</li>
 *   <li>{@code DELETE /auth/account} — delete account (authenticated)</li>
 * </ul>
 *
 * <p><b>Frontend consumption:</b> All endpoints are called from {@code src/lib/api/auth.ts}
 * in the React frontend via Axios with credentials enabled.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication, registration, and account management")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "drawpin_refresh_token";

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    @Value("${drawpin.jwt.refresh-token-expiry-seconds:1209600}")
    private int refreshTokenMaxAge;

    @Value("${drawpin.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new DrawPin account.
     *
     * <p>On success, sets the refresh token as an HttpOnly cookie and returns
     * the access token plus user profile in the body.
     * A verification email is dispatched asynchronously.
     *
     * @param request    validated registration payload
     * @param httpReq    to extract the User-Agent and IP
     * @param httpRes    to set the refresh token cookie
     * @return 201 with {@link AuthResponse}
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new account",
               description = "Creates a user account and sends an email verification link.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation errors"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        String deviceInfo = extractDeviceInfo(httpReq);
        String ipAddress = extractIpAddress(httpReq);

        AuthResult result = authService.register(request, deviceInfo, ipAddress);
        setRefreshTokenCookie(httpRes, result.getRawRefreshToken());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result.getAuthResponse()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates with email and password.
     *
     * <p>On success, sets the refresh token as an HttpOnly cookie and returns
     * the access token plus user profile.
     *
     * @param request validated login payload
     * @param httpReq to extract User-Agent and IP
     * @param httpRes to set the refresh token cookie
     * @return 200 with {@link AuthResponse}
     */
    @PostMapping("/login")
    @Operation(summary = "Log in to DrawPin",
               description = "Authenticates with email and password. Returns JWT access token and sets HttpOnly refresh cookie.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or account locked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation errors")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        String deviceInfo = extractDeviceInfo(httpReq);
        String ipAddress = extractIpAddress(httpReq);

        AuthResult result = authService.login(request, deviceInfo, ipAddress);
        setRefreshTokenCookie(httpRes, result.getRawRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok(result.getAuthResponse()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFRESH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Issues a new access token using the refresh token cookie.
     *
     * <p>The old refresh token is rotated (invalidated and replaced).
     * The new refresh token cookie is set automatically.
     *
     * @param httpReq to read the existing refresh cookie
     * @param httpRes to set the new refresh cookie
     * @return 200 with a fresh {@link AuthResponse}
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
               description = "Uses the HttpOnly refresh token cookie to issue a new short-lived access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        String rawRefreshToken = extractRefreshTokenCookie(httpReq);

        if (rawRefreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(
                            com.drawpin.dto.response.ErrorResponse.of(
                                    "MISSING_REFRESH_TOKEN", "No refresh token found. Please log in again.")));
        }

        AuthResult result = authService.refresh(rawRefreshToken);
        // Rotate the cookie — old token is revoked, new token is set
        setRefreshTokenCookie(httpRes, result.getRawRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok(result.getAuthResponse()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Logs out the current session.
     *
     * <p>Revokes the refresh token and clears the cookie.
     *
     * @param httpReq to extract the refresh cookie
     * @param httpRes to clear the refresh cookie
     * @return 200 with a success message
     */
    @PostMapping("/logout")
    @Operation(summary = "Log out",
               description = "Revokes the current session's refresh token and clears the cookie.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> logout(
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        String rawRefreshToken = extractRefreshTokenCookie(httpReq);
        authService.logout(rawRefreshToken);
        clearRefreshTokenCookie(httpRes);

        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Initiates the password reset flow.
     *
     * <p>Always responds 200 regardless of whether the email is registered
     * to prevent email enumeration attacks.
     *
     * @param request the email address
     * @return 200 with a generic message
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset",
               description = "Sends a reset email if the address is registered. Always returns 200.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        passwordResetService.initiateReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                "If this email is registered, you will receive a password reset link shortly."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESET PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies a new password using a valid reset token.
     *
     * @param request the token and new password
     * @return 200 on success
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password",
               description = "Validates the reset token and sets the new password, revoking all sessions.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
    })
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please log in."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VERIFY EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifies the user's email address using the token from the verification email.
     *
     * @param request the verification token
     * @return 200 on success
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address",
               description = "Marks the user's email as verified using the token from the verification email.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> verifyEmail(
            @RequestBody @Valid VerifyEmailRequest request) {
        emailVerificationService.verify(request.getToken());
        return ResponseEntity.ok(ApiResponse.ok("Email address verified successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Changes the authenticated user's password.
     *
     * @param request   the current + new password
     * @param principal the authenticated user
     * @return 200 on success
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password",
               description = "Changes password for the authenticated user. Revokes all other sessions.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @CurrentUser DrawPinUserDetails principal) {
        authService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET CURRENT USER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the authenticated user's profile.
     * Called by the frontend on application load to hydrate the auth context.
     *
     * @param principal the authenticated user
     * @return 200 with {@link UserResponse}
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user",
               description = "Returns the authenticated user's profile. Used to hydrate the frontend auth context.")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @CurrentUser DrawPinUserDetails principal) {
        UserResponse user = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE ACCOUNT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes the authenticated user's account.
     *
     * @param password  the account password for final confirmation
     * @param principal the authenticated user
     * @param httpRes   to clear the refresh cookie
     * @return 200 on success
     */
    @DeleteMapping("/account")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete account",
               description = "Soft-deletes the account. Requires password confirmation.")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> deleteAccount(
            @RequestParam String password,
            @CurrentUser DrawPinUserDetails principal,
            HttpServletResponse httpRes) {
        authService.deleteAccount(principal.getUserId(), password);
        clearRefreshTokenCookie(httpRes);
        return ResponseEntity.ok(ApiResponse.ok("Your account has been deleted."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets the refresh token as an HttpOnly, Secure, SameSite=Strict cookie.
     *
     * @param response the HTTP response to add the cookie to
     * @param rawToken the raw refresh token string
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(refreshTokenMaxAge);
        response.addCookie(cookie);
    }

    /**
     * Clears the refresh token cookie by setting its maxAge to 0.
     *
     * @param response the HTTP response to clear the cookie on
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Extracts the refresh token from the request's cookies.
     *
     * @param request the HTTP request
     * @return the raw refresh token string, or {@code null} if the cookie is absent
     */
    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts a short device info string from the User-Agent header.
     *
     * @param request the HTTP request
     * @return a truncated User-Agent string (max 255 chars)
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown Device";
        return ua.length() > 255 ? ua.substring(0, 255) : ua;
    }

    /**
     * Extracts the client IP, checking for X-Forwarded-For header first (reverse proxy support).
     *
     * @param request the HTTP request
     * @return the client IP address string
     */
    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
