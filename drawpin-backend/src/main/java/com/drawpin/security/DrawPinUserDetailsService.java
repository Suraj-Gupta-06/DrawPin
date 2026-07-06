package com.drawpin.security;

import com.drawpin.domain.entity.User;
import com.drawpin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation for DrawPin.
 *
 * <p>Spring Security calls {@link #loadUserByUsername(String)} during the standard
 * form-login flow and during Basic Auth. In DrawPin's JWT flow, this service is also
 * called by {@link JwtAuthenticationFilter} after validating the JWT — it uses the
 * email extracted from the token to load the full user entity, ensuring the user
 * still exists and is still active in the database on every authenticated request.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.config.SecurityConfig} — registered as the UserDetailsService bean</li>
 *   <li>{@link JwtAuthenticationFilter} — calls this after JWT validation</li>
 *   <li>{@link UserRepository} — the data source</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DrawPinUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address and wraps them in a {@link DrawPinUserDetails}.
     *
     * <p>Soft-deleted users are excluded via the {@code deletedAtIsNull} constraint.
     * Suspended users are not excluded here — they are loaded but
     * {@link DrawPinUserDetails#isEnabled()} returns {@code false}, causing Spring Security
     * to throw a {@code DisabledException}.
     *
     * @param email the user's email address (used as the Spring Security username)
     * @return a fully populated {@link DrawPinUserDetails}
     * @throws UsernameNotFoundException if no active user exists with this email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found with email: " + email
                ));
        return new DrawPinUserDetails(user);
    }
}
