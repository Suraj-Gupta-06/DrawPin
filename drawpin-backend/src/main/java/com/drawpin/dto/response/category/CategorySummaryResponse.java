package com.drawpin.dto.response.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal summary of a Category")
public class CategorySummaryResponse {

    @Schema(description = "Category UUID")
    private UUID id;

    @Schema(description = "URL-friendly slug")
    private String slug;

    @Schema(description = "Category name")
    private String name;
}
