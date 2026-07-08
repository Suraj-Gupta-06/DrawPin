package com.drawpin.controller.media;

import com.drawpin.domain.enums.MediaType;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.media.MediaResponse;
import com.drawpin.dto.response.media.MediaUploadResponse;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.media.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Central media management for DrawPin platform")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media", description = "Uploads an image or video to the storage provider.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MediaUploadResponse> uploadMedia(
            @AuthenticationPrincipal DrawPinUserDetails userDetails,
            @Parameter(description = "The file to upload") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Type of media (e.g. PIN_IMAGE, AVATAR)") @RequestParam("mediaType") MediaType mediaType) {
        
        MediaUploadResponse response = mediaService.uploadMedia(userDetails.getUserId(), file, mediaType);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete media", description = "Soft deletes a media asset.", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<ApiResponse.MessagePayload> deleteMedia(
            @AuthenticationPrincipal DrawPinUserDetails userDetails,
            @PathVariable UUID id) {
        
        mediaService.deleteMedia(id, userDetails.getUserId());
        return ApiResponse.ok("Media successfully deleted.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get media details", description = "Retrieves full details and variants for a media asset.")
    public ApiResponse<MediaResponse> getMediaById(@PathVariable UUID id) {
        return ApiResponse.ok(mediaService.getMediaById(id));
    }
}
