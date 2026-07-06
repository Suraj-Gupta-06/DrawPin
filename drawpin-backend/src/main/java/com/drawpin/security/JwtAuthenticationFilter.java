package com.drawpin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that intercepts every HTTP request and validates the JWT
 * from the {@code Authorization: Bearer} header.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee execution exactly once per
 * request, even in forward/include chains.
 *
 * <p><b>Flow per request:</b>
 * <ol>
 *   <li>Extract the Bearer token from the {@code Authorization} header.</li>
 *   <li>If no token is present, skip to the next filter (unauthenticated request).</li>
 *   <li>Validate the token with {@link JwtTokenProvider}.</li>
 *   <li>If valid, load the {@link DrawPinUserDetails} from the database.</li>
 *   <li>Set an {@link UsernamePasswordAuthenticationToken} on the {@link SecurityContextHolder}.</li>
 *   <li>Continue the filter chain.</li>
 * </ol>
 *
 * <p>Invalid or expired tokens are silently ignored here — the request continues as
 * unauthenticated, and Spring Security will return 401 if a protected endpoint is reached.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.config.SecurityConfig} — registered before
 *       {@code UsernamePasswordAuthenticationFilter}</li>
 *   <li>{@link JwtTokenProvider} — performs the actual JWT parsing and validation</li>
 *   <li>{@link DrawPinUserDetailsService} — loads the user from the database</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final DrawPinUserDetailsService userDetailsService;

    /**
     * Core filter logic executed once per request.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Bearer token from Authorization header
        String token = extractBearerToken(request);

        if (token == null) {
            // No token present — proceed as unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Validate the token
        JwtTokenProvider.JwtValidationResult validationResult = jwtTokenProvider.validate(token);

        if (validationResult != JwtTokenProvider.JwtValidationResult.VALID) {
            log.debug("JWT validation failed on {} {}: {}",
                    request.getMethod(), request.getRequestURI(), validationResult);
            // Let the request proceed unauthenticated — protected endpoints will return 401
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Token is valid — authenticate if no existing authentication is set
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtTokenProvider.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 4. Build authentication token and set it in the security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated user '{}' for {} {}",
                        email, request.getMethod(), request.getRequestURI());

            } catch (Exception e) {
                // User may have been deleted since the token was issued
                log.warn("Could not authenticate user from JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the {@code Authorization: Bearer <token>} header.
     *
     * @param request the HTTP request
     * @return the raw token string, or {@code null} if no valid Bearer token is present
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
