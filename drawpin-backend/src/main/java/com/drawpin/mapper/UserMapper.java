package com.drawpin.mapper;

import com.drawpin.domain.entity.User;
import com.drawpin.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    // creatorProfileId is handled manually or by decorators where needed
    @Mapping(target = "creatorProfileId", ignore = true)
    UserResponse toUserResponse(User user);

    @Named("roleToString")
    default String roleToString(Enum<?> role) {
        return role != null ? role.name().toLowerCase() : null;
    }
}
