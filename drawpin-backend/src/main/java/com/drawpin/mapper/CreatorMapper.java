package com.drawpin.mapper;

import com.drawpin.domain.entity.Creator;
import com.drawpin.dto.request.creator.BecomeCreatorRequest;
import com.drawpin.dto.request.creator.UpdateCreatorProfileRequest;
import com.drawpin.dto.response.creator.CreatorProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreatorMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "handle", source = "user.handle")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "coverUrl", source = "user.coverUrl")
    @Mapping(target = "verificationStatus", source = "verificationStatus")
    CreatorProfileResponse toResponse(Creator creator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "followersCount", ignore = true)
    @Mapping(target = "followingCount", ignore = true)
    @Mapping(target = "artworksCount", ignore = true)
    @Mapping(target = "reviewsCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    Creator toEntity(BecomeCreatorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "followersCount", ignore = true)
    @Mapping(target = "followingCount", ignore = true)
    @Mapping(target = "artworksCount", ignore = true)
    @Mapping(target = "reviewsCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    void updateEntityFromRequest(UpdateCreatorProfileRequest request, @MappingTarget Creator creator);
}
