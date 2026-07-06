package com.drawpin.controller.user;

import com.drawpin.dto.request.user.AddressRequest;
import com.drawpin.dto.response.AddressResponse;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.security.CurrentUser;
import com.drawpin.security.DrawPinUserDetails;
import com.drawpin.service.user.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "User Addresses", description = "Operations for managing user addresses")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all addresses for current user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @CurrentUser DrawPinUserDetails principal) {
        List<AddressResponse> response = addressService.getAddresses(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @CurrentUser DrawPinUserDetails principal,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.addAddress(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @CurrentUser DrawPinUserDetails principal,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(principal.getUserId(), addressId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<ApiResponse.MessagePayload>> deleteAddress(
            @CurrentUser DrawPinUserDetails principal,
            @PathVariable UUID addressId) {
        addressService.deleteAddress(principal.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully"));
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @CurrentUser DrawPinUserDetails principal,
            @PathVariable UUID addressId) {
        AddressResponse response = addressService.setDefaultAddress(principal.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
