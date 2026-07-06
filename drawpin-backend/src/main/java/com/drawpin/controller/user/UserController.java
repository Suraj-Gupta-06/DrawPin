package com.drawpin.controller.user;

import com.drawpin.dto.request.user.ChangeEmailRequest;
import com.drawpin.dto.request.user.UpdateProfileRequest;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.UserResponse;
import com.drawpin.security.CurrentUser;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations for user profiles, avatars, and emails")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @CurrentUser DrawPinUserDetails principal) {
        UserResponse response = userService.getProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser DrawPinUserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{handle}")
    @Operation(summary = "Get public profile by handle")
    public ResponseEntity<ApiResponse<UserResponse>> getPublicProfile(
            @PathVariable String handle) {
        UserResponse response = userService.getPublicProfile(handle);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/me/email")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Request email change")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> requestEmailChange(
            @CurrentUser DrawPinUserDetails principal,
            @Valid @RequestBody ChangeEmailRequest request) {
        userService.requestEmailChange(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Verification email sent to new address"));
    }

    @PutMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload or replace avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @CurrentUser DrawPinUserDetails principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        UserResponse response = userService.updateAvatar(principal.getUserId(), file);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete avatar")
    public ResponseEntity<ApiResponse<UserResponse>> deleteAvatar(
            @CurrentUser DrawPinUserDetails principal) {
        UserResponse response = userService.deleteAvatar(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
