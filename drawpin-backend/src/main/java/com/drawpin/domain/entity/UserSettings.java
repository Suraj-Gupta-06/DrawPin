package com.drawpin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity storing per-user preference flags.
 *
 * <p>Every {@link User} has exactly one {@code UserSettings} row, automatically
 * created by {@link com.drawpin.service.auth.AuthService} at registration time
 * using default values.
 *
 * <p><b>Table:</b> {@code user_settings}<br>
 * <b>Managed by Flyway:</b> {@code V1__create_users.sql}
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link User} — one-to-one, mandatory FK</li>
 *   <li>{@link com.drawpin.repository.UserSettingsRepository}</li>
 * </ul>
 *
 * <p><b>Frontend APIs that consume this entity:</b>
 * <ul>
 *   <li>{@code GET /api/v1/users/me/settings}</li>
 *   <li>{@code PATCH /api/v1/users/me/settings}</li>
 * </ul>
 */
@Entity
@Table(name = "user_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** FK to the owning user. Unique — enforces the one-to-one constraint. */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /** Whether this user wants to receive email notifications for platform events. */
    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private boolean emailNotifications = true;

    /** Whether this user wants push notifications (future mobile/PWA support). */
    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private boolean pushNotifications = true;

    /** When {@code true}, the user's profile is not discoverable in search or listings. */
    @Column(name = "private_profile", nullable = false)
    @Builder.Default
    private boolean privateProfile = false;

    /** Whether the user's "Active now" status is visible to other users in chat. */
    @Column(name = "show_online_status", nullable = false)
    @Builder.Default
    private boolean showOnlineStatus = true;

    /** Whether other users are permitted to send this user direct messages. */
    @Column(name = "allow_messages", nullable = false)
    @Builder.Default
    private boolean allowMessages = true;

    /** UI colour scheme preference: {@code dark}, {@code light}, or {@code system}. */
    @Column(name = "theme", nullable = false, length = 10)
    @Builder.Default
    private String theme = "dark";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
