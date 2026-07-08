package com.drawpin.controller.creator;

import com.drawpin.dto.request.creator.BecomeCreatorRequest;
import com.drawpin.dto.request.creator.UpdateCreatorProfileRequest;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.creator.CreatorProfileResponse;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.creator.CreatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/creators")
@RequiredArgsConstructor
@Tag(name = "Creators", description = "Creator profile management and public viewing APIs")
public class CreatorController {

    private final CreatorService creatorService;

    @PostMapping("/become")
    @Operation(summary = "Become a creator", description = "Upgrades the authenticated user to a Creator and provisions a profile.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatorProfileResponse> becomeCreator(
            @AuthenticationPrincipal DrawPinUserDetails userDetails,
            @Valid @RequestBody BecomeCreatorRequest request) {
        CreatorProfileResponse response = creatorService.becomeCreator(userDetails.getUserId(), request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update creator profile", description = "Updates the authenticated creator's profile.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ApiResponse<CreatorProfileResponse> updateMyProfile(
            @AuthenticationPrincipal DrawPinUserDetails userDetails,
            @Valid @RequestBody UpdateCreatorProfileRequest request) {
        CreatorProfileResponse response = creatorService.updateProfile(userDetails.getUserId(), request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/me/verify")
    @Operation(summary = "Request verification", description = "Requests admin verification for the creator profile.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('CREATOR')")
    public ApiResponse<ApiResponse.MessagePayload> requestVerification(
            @AuthenticationPrincipal DrawPinUserDetails userDetails) {
        creatorService.requestVerification(userDetails.getUserId());
        return ApiResponse.ok("Verification request submitted");
    }

    @GetMapping("/{creatorId}")
    @Operation(summary = "Get creator profile by ID", description = "Public endpoint to view a creator's profile.")
    public ApiResponse<CreatorProfileResponse> getProfileById(
            @PathVariable UUID creatorId) {
        CreatorProfileResponse response = creatorService.getProfile(creatorId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/handle/{handle}")
    @Operation(summary = "Get creator profile by handle", description = "Public endpoint to view a creator's profile using their unique user handle.")
    public ApiResponse<CreatorProfileResponse> getProfileByHandle(
            @PathVariable String handle) {
        CreatorProfileResponse response = creatorService.getProfileByHandle(handle);
        return ApiResponse.ok(response);
    }
}
