package com.drawpin.service.auth;

import com.drawpin.domain.entity.User;
import com.drawpin.dto.response.AuthResponse;
import com.drawpin.dto.response.UserResponse;
import com.drawpin.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating JWT access tokens and building
 * the {@link AuthResponse} envelope returned after login and registration.
 *
 * <p>This is intentionally a thin wrapper over {@link JwtTokenProvider} — keeping
 * token generation logic in one place makes it easy to swap the JWT library or
 * algorithm in the future.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link JwtTokenProvider} — delegates actual token signing</li>
 *   <li>{@link AuthService} — calls this after successful authentication</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Generates a signed JWT access token for the given user.
     *
     * @param user the authenticated user entity
     * @return a compact JWT string (header.payload.signature)
     */
    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user);
    }

    /**
     * Builds the full {@link AuthResponse} payload returned to the frontend
     * on login, register, and refresh operations.
     *
     * <p>The refresh token is NOT included here — it is set as an HttpOnly cookie
     * by the controller layer.
     *
     * @param user         the authenticated user
     * @param userResponse the pre-mapped user profile DTO
     * @return a fully populated {@link AuthResponse}
     */
    public AuthResponse buildAuthResponse(User user, UserResponse userResponse) {
        String accessToken = generateAccessToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .user(userResponse)
                .build();
    }
}
