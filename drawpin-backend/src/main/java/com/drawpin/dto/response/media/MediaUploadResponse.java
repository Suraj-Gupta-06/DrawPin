package com.drawpin.dto.response.media;

import com.drawpin.domain.enums.MediaType;
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
@Schema(description = "Response returned immediately after a successful media upload")
public class MediaUploadResponse {
    
    @Schema(description = "Media UUID")
    private UUID id;
    
    @Schema(description = "Primary URL to access the media")
    private String secureUrl;
    
    @Schema(description = "Type of media")
    private MediaType mediaType;
}
