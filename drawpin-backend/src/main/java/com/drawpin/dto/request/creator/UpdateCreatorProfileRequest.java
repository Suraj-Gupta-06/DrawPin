package com.drawpin.dto.request.creator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to update creator profile")
public class UpdateCreatorProfileRequest {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 80, message = "Display name must be between 2 and 80 characters")
    @Schema(description = "Public display name", example = "Aria Vance Studio")
    private String displayName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Schema(description = "Creator bio", example = "Digital artist specializing in surrealism.")
    private String bio;

    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    @Schema(description = "Primary specialization", example = "3D Animator")
    private String specialization;

    @Schema(description = "Years of experience", example = "5")
    private Integer experienceYears;

    @Size(max = 10, message = "Maximum 10 skills allowed")
    @Schema(description = "List of skills", example = "[\"Blender\", \"Maya\"]")
    private List<String> skills;

    @Pattern(regexp = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$", message = "Invalid website URL")
    @Schema(description = "Portfolio website URL", example = "https://aria.studio")
    private String portfolioWebsite;

    @Schema(description = "Map of social links", example = "{\"instagram\": \"aria_v\", \"twitter\": \"aria_v\"}")
    private Map<String, String> socialLinks;

    @Schema(description = "Availability status for freelance work", example = "true")
    private Boolean isAvailable;
}
