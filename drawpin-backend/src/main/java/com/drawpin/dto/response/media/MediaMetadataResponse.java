package com.drawpin.dto.response.media;

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
@Schema(description = "Simplified metadata response for a Media asset")
public class MediaMetadataResponse {

    @Schema(description = "Media UUID")
    private UUID id;
    
    @Schema(description = "Width in pixels")
    private Integer width;
    
    @Schema(description = "Height in pixels")
    private Integer height;
    
    @Schema(description = "Calculated aspect ratio (width / height)")
    private Double aspectRatio;
    
    @Schema(description = "Orientation (e.g. landscape, portrait, square)")
    private String orientation;
    
    @Schema(description = "Duration in seconds (for video/audio)")
    private Double duration;
    
    @Schema(description = "File size in bytes")
    private Long fileSize;
}
