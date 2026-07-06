package com.drawpin.repository;

import com.drawpin.domain.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link UserSettings}.
 *
 * <p>Settings are always created alongside the user (in the same transaction),
 * so the only access pattern needed is "load by user ID".
 *
 * <p><b>Connected to:</b>
 * <ul>
 *   <li>{@link com.drawpin.service.auth.AuthService} — creates the settings row on registration</li>
 *   <li>{@link com.drawpin.service.UserService} — reads and updates settings</li>
 * </ul>
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    /**
     * Finds the settings row for a given user.
     *
     * @param userId the user's UUID
     * @return an {@link Optional} containing the settings if found
     */
    Optional<UserSettings> findByUserId(UUID userId);
}
