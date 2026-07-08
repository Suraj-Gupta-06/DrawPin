package com.drawpin.dto.response.creator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public Creator Profile Response")
public class CreatorProfileResponse {

    @Schema(description = "Creator Profile ID")
    private UUID id;

    @Schema(description = "User ID of the creator")
    private UUID userId;

    @Schema(description = "User handle/username")
    private String handle;

    @Schema(description = "Avatar URL from Cloudinary")
    private String avatarUrl;
    
    @Schema(description = "Cover URL from Cloudinary")
    private String coverUrl;

    @Schema(description = "Display name")
    private String displayName;

    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Specialization")
    private String specialization;

    @Schema(description = "Years of experience")
    private Integer experienceYears;

    @Schema(description = "Skills")
    private List<String> skills;

    @Schema(description = "Portfolio website")
    private String portfolioWebsite;

    @Schema(description = "Social links")
    private Map<String, String> socialLinks;

    @Schema(description = "Availability status")
    private boolean isAvailable;

    @Schema(description = "Verification status (NONE, PENDING, APPROVED, REJECTED)")
    private String verificationStatus;

    @Schema(description = "Total followers count")
    private int followersCount;

    @Schema(description = "Total following count")
    private int followingCount;

    @Schema(description = "Total artworks/pins count")
    private int artworksCount;

    @Schema(description = "Total reviews count")
    private int reviewsCount;

    @Schema(description = "Average rating")
    private double averageRating;
}
