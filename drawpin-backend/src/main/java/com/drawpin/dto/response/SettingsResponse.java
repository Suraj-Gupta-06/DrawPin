package com.drawpin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettingsResponse {
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean privateProfile;
    private boolean showOnlineStatus;
    private boolean allowMessages;
    private String theme;
}
