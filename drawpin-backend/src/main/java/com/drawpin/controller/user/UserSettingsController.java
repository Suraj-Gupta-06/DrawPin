package com.drawpin.controller.user;

import com.drawpin.dto.request.user.UpdateSettingsRequest;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.SettingsResponse;
import com.drawpin.security.CurrentUser;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.user.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/settings")
@RequiredArgsConstructor
@Tag(name = "User Settings", description = "Operations for user preferences and settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user settings")
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings(
            @CurrentUser DrawPinUserDetails principal) {
        SettingsResponse response = userSettingsService.getSettings(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user settings")
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @CurrentUser DrawPinUserDetails principal,
            @Valid @RequestBody UpdateSettingsRequest request) {
        SettingsResponse response = userSettingsService.updateSettings(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
