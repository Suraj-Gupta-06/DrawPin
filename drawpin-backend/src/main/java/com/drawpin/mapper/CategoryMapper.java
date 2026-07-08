package com.drawpin.mapper;

import com.drawpin.domain.entity.Category;
import com.drawpin.dto.request.category.CreateCategoryRequest;
import com.drawpin.dto.request.category.UpdateCategoryRequest;
import com.drawpin.dto.response.category.CategoryResponse;
import com.drawpin.dto.response.category.CategorySummaryResponse;
import com.drawpin.dto.response.category.CategoryTreeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentSlug", source = "parent.slug")
    CategoryResponse toResponse(Category category);

    CategorySummaryResponse toSummaryResponse(Category category);

    CategoryTreeResponse toTreeResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "active", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "slug", ignore = true) // Handle slug updates carefully in service
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category category);
}
