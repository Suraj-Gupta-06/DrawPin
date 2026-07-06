package com.drawpin.mapper;

import com.drawpin.domain.entity.UserSettings;
import com.drawpin.dto.request.user.UpdateSettingsRequest;
import com.drawpin.dto.response.SettingsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SettingsMapper {

    SettingsResponse toResponse(UserSettings userSettings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateSettingsRequest request, @MappingTarget UserSettings settings);
}
