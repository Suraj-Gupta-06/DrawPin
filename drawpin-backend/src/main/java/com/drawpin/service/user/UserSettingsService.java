package com.drawpin.service.user;

import com.drawpin.domain.entity.UserSettings;
import com.drawpin.dto.request.user.UpdateSettingsRequest;
import com.drawpin.dto.response.SettingsResponse;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.mapper.SettingsMapper;
import com.drawpin.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final SettingsMapper settingsMapper;

    /**
     * Gets the settings for the current user.
     */
    @Transactional(readOnly = true)
    public SettingsResponse getSettings(UUID userId) {
        UserSettings settings = getEntity(userId);
        return settingsMapper.toResponse(settings);
    }

    /**
     * Updates the user's settings.
     */
    public SettingsResponse updateSettings(UUID userId, UpdateSettingsRequest request) {
        UserSettings settings = getEntity(userId);
        
        settingsMapper.updateEntityFromRequest(request, settings);
        settings = userSettingsRepository.save(settings);
        
        log.debug("Updated settings for user {}", userId);
        return settingsMapper.toResponse(settings);
    }
    
    private UserSettings getEntity(UUID userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("SETTINGS_NOT_FOUND", "User settings not found."));
    }
}
