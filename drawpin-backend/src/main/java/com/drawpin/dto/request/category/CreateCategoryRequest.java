package com.drawpin.dto.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create a new category")
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Display name of the category", example = "Digital Art")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
    @Schema(description = "URL-friendly unique identifier", example = "digital-art")
    private String slug;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Detailed description", example = "All forms of digital illustration and 3D modeling")
    private String description;

    @Schema(description = "Parent category UUID (if this is a subcategory)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID parentId;

    @Schema(description = "Icon identifier or URL", example = "icon-brush")
    private String icon;

    @Schema(description = "Banner/Thumbnail image URL", example = "https://res.cloudinary.com/drawpin/category_art.jpg")
    private String imageUrl;

    @Schema(description = "Order for UI sorting", example = "10")
    @Builder.Default
    private Integer displayOrder = 0;
}
