package com.drawpin.domain.enums;

/**
 * Defines the access roles available on the DrawPin platform.
 *
 * <p>Roles are stored as strings in the {@code users.role} column and embedded in the
 * JWT payload for stateless authorisation checks on every request.
 *
 * <p><b>Role hierarchy (highest → lowest privileges):</b>
 * <pre>
 *   ADMIN > MODERATOR > CREATOR > COLLECTOR
 * </pre>
 *
 * <p>Spring Security uses the {@code ROLE_} prefix convention. The prefix is added
 * automatically by {@link com.drawpin.security.DrawPinUserDetails} when building the
 * {@code GrantedAuthority} list.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.domain.entity.User} — stored in the {@code role} column</li>
 *   <li>{@link com.drawpin.security.DrawPinUserDetails} — converted to Spring Security authorities</li>
 *   <li>{@link com.drawpin.config.SecurityConfig} — referenced in {@code @PreAuthorize} rules</li>
 * </ul>
 */
public enum UserRole {

    /** Standard registered user. Can browse, save, follow, and place orders. */
    COLLECTOR,

    /** Verified creator. Can also post pins, list services, and receive orders. */
    CREATOR,

    /** Platform moderator. Can review reports and suspend users. */
    MODERATOR,

    /** Full platform administrator. Unrestricted access to all operations. */
    ADMIN
}
