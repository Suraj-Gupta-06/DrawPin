package com.drawpin.dto.response.media;

import com.drawpin.domain.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed representation of a Media asset")
public class MediaResponse {

    @Schema(description = "Media UUID")
    private UUID id;
    
    @Schema(description = "Type of media")
    private MediaType mediaType;
    
    @Schema(description = "Original uploaded filename")
    private String originalFilename;
    
    @Schema(description = "Primary secure URL")
    private String secureUrl;
    
    @Schema(description = "Thumbnail URL, if applicable")
    private String thumbnailUrl;
    
    @Schema(description = "Dictionary of different image variants (e.g. small, medium, large, webp)")
    private Map<String, String> variants;
    
    @Schema(description = "MIME type")
    private String mimeType;
    
    @Schema(description = "File size in bytes")
    private Long fileSize;
    
    @Schema(description = "Creation timestamp")
    private Instant createdAt;
}
