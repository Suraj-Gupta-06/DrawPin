package com.drawpin.controller.category;

import com.drawpin.dto.request.category.CreateCategoryRequest;
import com.drawpin.dto.request.category.UpdateCategoryRequest;
import com.drawpin.dto.response.ApiResponse;
import com.drawpin.dto.response.category.CategoryResponse;
import com.drawpin.dto.response.category.CategoryTreeResponse;
import com.drawpin.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category hierarchy and reference data APIs")
public class CategoryController {

    private final CategoryService categoryService;

    // --- Admin Endpoints (Mutations) ---

    @PostMapping
    @Operation(summary = "Create category", description = "Creates a new category. Max depth is 4. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category. Validates for circular dependencies. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Soft deletes a category by setting it inactive. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ApiResponse.MessagePayload> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok("Category successfully deleted.");
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable category", description = "Re-enables a disabled category. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> enableCategory(@PathVariable UUID id) {
        return ApiResponse.ok(categoryService.enableCategory(id));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable category", description = "Disables a category. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> disableCategory(@PathVariable UUID id) {
        return ApiResponse.ok(categoryService.disableCategory(id));
    }

    // --- Public Endpoints (Reads) ---

    @GetMapping
    @Operation(summary = "Get all root categories", description = "Fetches all top-level categories without parents.")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        return ApiResponse.ok(categoryService.getRootCategories());
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Fetches the entire category hierarchy in a nested tree format.")
    public ApiResponse<List<CategoryTreeResponse>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Fetches a flat representation of a single category.")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ApiResponse.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Fetches a flat representation of a single category by its slug.")
    public ApiResponse<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ApiResponse.ok(categoryService.getCategoryBySlug(slug));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Fetches the direct children of a specific category.")
    public ApiResponse<List<CategoryResponse>> getChildCategories(@PathVariable UUID id) {
        return ApiResponse.ok(categoryService.getChildren(id));
    }
}
