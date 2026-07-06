package com.drawpin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Internal (non-JSON) result object that bundles the {@link AuthResponse} and the raw
 * refresh token together so the service layer can return both without exposing the
 * raw token in the public API response body.
 *
 * <p>The controller extracts the {@code rawRefreshToken} and sets it as an
 * HttpOnly cookie, then returns only the {@link AuthResponse} in the HTTP body.
 *
 * <p>This class is NOT serialised to JSON — it is only used internally between
 * {@link com.drawpin.service.auth.AuthService} and
 * {@link com.drawpin.controller.auth.AuthController}.
 */
@Getter
@AllArgsConstructor
public class AuthResult {

    /** The public-facing authentication response (access token + user profile). */
    private final AuthResponse authResponse;

    /**
     * The raw refresh token to be set as an HttpOnly cookie.
     * Never serialised to JSON — controller only.
     */
    private final String rawRefreshToken;
}
