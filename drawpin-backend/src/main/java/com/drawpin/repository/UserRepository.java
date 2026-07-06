package com.drawpin.repository;

import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.UserRole;
import com.drawpin.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link User} entity.
 *
 * <p>All database access for the {@code users} table flows through this interface.
 * The service layer ({@link com.drawpin.service.auth.AuthService},
 * {@link com.drawpin.service.UserService}) is the only consumer — controllers never
 * call repositories directly.
 *
 * <p><b>Query methods follow the Spring Data naming convention</b> where possible.
 * Custom JPQL is used only when the derived method would be ambiguous or inefficient.
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.AuthService} — register, login, delete account</li>
 *   <li>{@link com.drawpin.service.auth.PasswordResetService} — fetch user for password update</li>
 *   <li>{@link com.drawpin.security.DrawPinUserDetailsService} — load user by email for Spring Security</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a non-deleted user by email address.
     * Used by Spring Security during authentication and by the registration flow for uniqueness checks.
     *
     * @param email the email address to search for (case-sensitive)
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Finds a non-deleted user by their unique handle.
     * Used for profile page lookups and handle uniqueness validation.
     *
     * @param handle the handle to search for (e.g., {@code aria.vance})
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByHandleAndDeletedAtIsNull(String handle);

    /**
     * Checks whether an email address is already registered.
     * Used during registration to return a fast {@code 409 Conflict} without loading the full entity.
     *
     * @param email the email address to check
     * @return {@code true} if a user exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a handle is already in use.
     * Used during registration handle generation and profile edit to detect conflicts.
     *
     * @param handle the handle to check
     * @return {@code true} if a user exists with this handle
     */
    boolean existsByHandle(String handle);

    /**
     * Finds a non-deleted user by their primary key.
     * This override excludes soft-deleted users from normal lookups.
     *
     * @param id the user UUID
     * @return an {@link Optional} containing the user if found and not deleted
     */
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Paginated search across users for the admin user management table.
     * Filters by optional query string (name or handle), role, and status.
     *
     * @param query    optional search string matched against name and handle
     * @param role     optional role filter
     * @param status   optional status filter
     * @param pageable pagination parameters
     * @return a page of users matching the criteria
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.deletedAt IS NULL
              AND (:query IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
                                  OR LOWER(u.handle) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(
            @Param("query") String query,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    /**
     * Increments the failed login attempt counter for a user.
     * Called on every failed password validation.
     *
     * @param userId the user's UUID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = u.failedLoginCount + 1 WHERE u.id = :userId")
    void incrementFailedLoginCount(@Param("userId") UUID userId);

    /**
     * Resets failed login counters and removes the lockout timestamp on successful login.
     *
     * @param userId the user's UUID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = 0, u.lockedUntil = NULL WHERE u.id = :userId")
    void resetLoginFailures(@Param("userId") UUID userId);

    /**
     * Applies an account lockout by setting the {@code locked_until} timestamp.
     * Called when failed login count reaches the configured threshold.
     *
     * @param userId      the user's UUID
     * @param lockedUntil the timestamp until which login is blocked
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") UUID userId, @Param("lockedUntil") Instant lockedUntil);

    /**
     * Marks the email address as verified for a user.
     *
     * @param userId the user's UUID
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = TRUE WHERE u.id = :userId")
    void markEmailVerified(@Param("userId") UUID userId);
}
