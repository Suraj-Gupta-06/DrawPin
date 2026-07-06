package com.drawpin.security;

import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.domain.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} implementation that wraps a {@link User} entity.
 *
 * <p>This class bridges the DrawPin domain model and the Spring Security authentication
 * framework. It is returned by {@link DrawPinUserDetailsService#loadUserByUsername}
 * and stored in the {@code SecurityContext} after successful JWT validation.
 *
 * <p>The {@link com.drawpin.security.JwtAuthenticationFilter} creates an
 * {@code UsernamePasswordAuthenticationToken} from this object after validating the JWT.
 *
 * <p><b>Authority convention:</b> Spring Security expects authorities prefixed with
 * {@code ROLE_}. So {@code CREATOR} becomes {@code ROLE_CREATOR}. The prefix is added
 * here automatically and used in {@code @PreAuthorize("hasRole('CREATOR')")} expressions.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link DrawPinUserDetailsService} — creates this object</li>
 *   <li>{@link JwtAuthenticationFilter} — sets this in the security context</li>
 *   <li>{@link com.drawpin.config.SecurityConfig} — referenced by method security</li>
 * </ul>
 */
@Getter
public class DrawPinUserDetails implements UserDetails {

    /** The wrapped domain entity. */
    private final User user;

    /** Pre-computed authority list. */
    private final List<GrantedAuthority> authorities;

    /**
     * Constructs a {@code DrawPinUserDetails} from a {@link User} entity.
     *
     * @param user the fully loaded user entity from the database
     */
    public DrawPinUserDetails(User user) {
        this.user = user;
        this.authorities = buildAuthorities(user.getRole());
    }

    /**
     * Returns the Spring Security authorities for this user.
     * The role is prefixed with {@code ROLE_} as required by Spring Security.
     *
     * @return immutable list containing exactly one {@link SimpleGrantedAuthority}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** Returns the BCrypt-hashed password. Spring Security uses this for password comparison. */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /** Returns the email address as the username (Spring Security identifier). */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /** Always {@code true} — account expiry is not used; {@link UserStatus} handles lifecycle. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Returns {@code false} when the account is temporarily locked due to failed logins.
     * Spring Security will reject authentication attempts for locked accounts.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !user.isAccountLocked();
    }

    /** Always {@code true} — credentials do not expire in this system. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Returns {@code false} when the account is suspended or soft-deleted.
     * Spring Security will reject authentication for disabled accounts.
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE && !user.isDeleted();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONVENIENCE ACCESSORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convenience method to get the user's UUID without going through {@code getUser()}.
     *
     * @return the user's primary key UUID
     */
    public UUID getUserId() {
        return user.getId();
    }

    /**
     * Convenience method to get the user's role.
     *
     * @return the {@link UserRole}
     */
    public UserRole getRole() {
        return user.getRole();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the Spring Security authority list from a {@link UserRole}.
     *
     * @param role the user's role
     * @return a list containing a single authority with the {@code ROLE_} prefix
     */
    private List<GrantedAuthority> buildAuthorities(UserRole role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
