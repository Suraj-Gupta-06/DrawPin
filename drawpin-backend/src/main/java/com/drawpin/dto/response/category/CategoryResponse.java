package com.drawpin.dto.response.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flat representation of a Category")
public class CategoryResponse {

    @Schema(description = "Category UUID")
    private UUID id;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Icon identifier")
    private String icon;

    @Schema(description = "Banner image URL")
    private String imageUrl;

    @Schema(description = "Sorting order")
    private Integer displayOrder;

    @Schema(description = "Is the category active (visible to public)")
    private boolean isActive;

    @Schema(description = "Parent category ID, null if it is a root category")
    private UUID parentId;
    
    @Schema(description = "Parent category slug, null if it is a root category")
    private String parentSlug;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
