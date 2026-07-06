package com.drawpin.domain.enums;

/**
 * Represents the lifecycle state of a user account.
 *
 * <p>Status is stored in the {@code users.status} column and checked on every login
 * attempt. Suspended and deleted users cannot authenticate.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.domain.entity.User} — stored in the {@code status} column</li>
 *   <li>{@link com.drawpin.service.auth.AuthService} — checked during login validation</li>
 *   <li>{@code POST /api/v1/admin/users/{id}/status} — admin status change endpoint</li>
 * </ul>
 */
public enum UserStatus {

    /** Account is in good standing. Login and API access permitted. */
    ACTIVE,

    /** Account has been suspended by a moderator or admin.
     *  All refresh tokens are revoked at the time of suspension. */
    SUSPENDED,

    /** Account has been soft-deleted by the user or admin.
     *  Data is retained but the account cannot log in. */
    DELETED
}
