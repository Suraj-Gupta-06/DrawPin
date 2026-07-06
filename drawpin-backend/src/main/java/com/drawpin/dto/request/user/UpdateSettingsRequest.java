package com.drawpin.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSettingsRequest {
    @NotNull(message = "Email notifications flag is required")
    private Boolean emailNotifications;

    @NotNull(message = "Push notifications flag is required")
    private Boolean pushNotifications;

    @NotNull(message = "Private profile flag is required")
    private Boolean privateProfile;

    @NotNull(message = "Show online status flag is required")
    private Boolean showOnlineStatus;

    @NotNull(message = "Allow messages flag is required")
    private Boolean allowMessages;

    @NotBlank(message = "Theme is required")
    private String theme;
}
