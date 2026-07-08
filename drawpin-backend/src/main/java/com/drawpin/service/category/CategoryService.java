package com.drawpin.service.category;

import com.drawpin.domain.entity.Category;
import com.drawpin.dto.request.category.CreateCategoryRequest;
import com.drawpin.dto.request.category.UpdateCategoryRequest;
import com.drawpin.dto.response.category.CategoryResponse;
import com.drawpin.dto.response.category.CategoryTreeResponse;
import com.drawpin.exception.ConflictException;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.exception.ValidationException;
import com.drawpin.mapper.CategoryMapper;
import com.drawpin.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    private static final int MAX_HIERARCHY_DEPTH = 4;

    /**
     * Creates a new category.
     */
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("SLUG_ALREADY_EXISTS", "A category with this slug already exists.");
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(request.getSlug().toLowerCase());
        category.setActive(true);

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            validateDepth(parent, 1); // Creating a child under this parent adds 1 level
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("AUDIT: Category created with ID {} and slug {}", category.getId(), category.getSlug());
        
        return categoryMapper.toResponse(category);
    }

    /**
     * Updates an existing category.
     */
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = getCategoryEntity(id);

        if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new ConflictException("SLUG_ALREADY_EXISTS", "A category with this slug already exists.");
        }

        categoryMapper.updateEntityFromRequest(request, category);
        category.setSlug(request.getSlug().toLowerCase());

        // Handle parent update
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ValidationException("INVALID_PARENT", "A category cannot be its own parent.");
            }
            Category newParent = getCategoryEntity(request.getParentId());
            
            // Prevent circular relationship (A -> B -> A)
            if (isDescendant(newParent, category)) {
                throw new ValidationException("CIRCULAR_RELATIONSHIP", "Cannot set a descendant as a parent.");
            }
            
            // Validate max depth
            validateDepth(newParent, getSubtreeDepth(category));
            
            category.setParent(newParent);
        } else {
            category.setParent(null); // Make it a root category
        }

        category = categoryRepository.save(category);
        log.info("AUDIT: Category updated with ID {}", category.getId());
        
        return categoryMapper.toResponse(category);
    }

    /**
     * Soft deletes a category (Sets isActive = false).
     */
    public void deleteCategory(UUID id) {
        Category category = getCategoryEntity(id);
        category.setActive(false);
        categoryRepository.save(category);
        log.info("AUDIT: Category soft-deleted (disabled) with ID {}", id);
    }

    /**
     * Enables a category.
     */
    public CategoryResponse enableCategory(UUID id) {
        Category category = getCategoryEntity(id);
        category.setActive(true);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /**
     * Disables a category.
     */
    public CategoryResponse disableCategory(UUID id) {
        Category category = getCategoryEntity(id);
        category.setActive(false);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /**
     * Retrieves a flat category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        return categoryMapper.toResponse(getCategoryEntity(id));
    }

    /**
     * Retrieves a flat category by slug.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found."));
        return categoryMapper.toResponse(category);
    }

    /**
     * Retrieves all root categories (categories without a parent).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves direct children of a given parent category.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildren(UUID parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Parent category not found.");
        }
        return categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the entire category tree, starting from roots.
     */
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toTreeResponse)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    private Category getCategoryEntity(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found with ID: " + id));
    }

    /**
     * Calculates the depth from a given parent up to the root, adding the depth of the incoming subtree.
     * Throws an exception if total depth > MAX_HIERARCHY_DEPTH.
     */
    private void validateDepth(Category parent, int incomingSubtreeDepth) {
        int depth = 1; // 1 for the parent itself
        Category current = parent;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        
        if (depth + incomingSubtreeDepth > MAX_HIERARCHY_DEPTH) {
            throw new ValidationException("MAX_DEPTH_EXCEEDED", "Category hierarchy cannot exceed " + MAX_HIERARCHY_DEPTH + " levels.");
        }
    }

    /**
     * Calculates the max depth of a category's subtree.
     * Leaf node = 1.
     */
    private int getSubtreeDepth(Category category) {
        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return 1;
        }
        return 1 + category.getChildren().stream()
                .mapToInt(this::getSubtreeDepth)
                .max()
                .orElse(0);
    }

    /**
     * Checks if 'potentialDescendant' is deeply nested under 'category'.
     * Used to prevent circular references (e.g., setting A's parent to B, when B is a child of A).
     */
    private boolean isDescendant(Category potentialDescendant, Category category) {
        Category current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
