package com.drawpin.dto.request.creator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to become a creator")
public class BecomeCreatorRequest {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 80, message = "Display name must be between 2 and 80 characters")
    @Schema(description = "Public display name for the creator profile", example = "Aria Vance Studio")
    private String displayName;

    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    @Schema(description = "Primary specialization", example = "3D Animator")
    private String specialization;

    @Pattern(regexp = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$", message = "Invalid website URL")
    @Schema(description = "Portfolio website URL", example = "https://aria.studio")
    private String portfolioWebsite;
}
