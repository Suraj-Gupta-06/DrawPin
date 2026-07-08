package com.drawpin.service.category;

import com.drawpin.domain.entity.Category;
import com.drawpin.dto.request.category.UpdateCategoryRequest;
import com.drawpin.exception.ValidationException;
import com.drawpin.mapper.CategoryMapper;
import com.drawpin.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;
    private Category childCategory;
    private Category grandChildCategory;

    @BeforeEach
    void setUp() {
        parentCategory = Category.builder().id(UUID.randomUUID()).slug("parent").build();
        
        childCategory = Category.builder().id(UUID.randomUUID()).slug("child").parent(parentCategory).build();
        parentCategory.getChildren().add(childCategory);
        
        grandChildCategory = Category.builder().id(UUID.randomUUID()).slug("grandchild").parent(childCategory).build();
        childCategory.getChildren().add(grandChildCategory);
    }

    @Test
    void updateCategory_WhenSelfReferencing_ThrowsValidationException() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .slug("parent")
                .parentId(parentCategory.getId())
                .build();

        when(categoryRepository.findById(parentCategory.getId())).thenReturn(Optional.of(parentCategory));

        ValidationException ex = assertThrows(ValidationException.class, 
                () -> categoryService.updateCategory(parentCategory.getId(), request));
        
        assertEquals("A category cannot be its own parent.", ex.getMessage());
    }

    @Test
    void updateCategory_WhenCircularReference_ThrowsValidationException() {
        // Attempt to make the Parent a child of its Grandchild (A -> B -> C -> A)
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .slug("parent")
                .parentId(grandChildCategory.getId())
                .build();

        when(categoryRepository.findById(parentCategory.getId())).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findById(grandChildCategory.getId())).thenReturn(Optional.of(grandChildCategory));

        ValidationException ex = assertThrows(ValidationException.class, 
                () -> categoryService.updateCategory(parentCategory.getId(), request));
        
        assertEquals("Cannot set a descendant as a parent.", ex.getMessage());
    }

    @Test
    void createCategory_WhenMaxDepthExceeded_ThrowsValidationException() {
        // Add a 4th level category
        Category greatGrandChild = Category.builder().id(UUID.randomUUID()).slug("great").parent(grandChildCategory).build();
        grandChildCategory.getChildren().add(greatGrandChild);

        // Attempting to add a 5th level (under greatGrandChild) should fail
        com.drawpin.dto.request.category.CreateCategoryRequest request = 
                com.drawpin.dto.request.category.CreateCategoryRequest.builder()
                .slug("too-deep")
                .parentId(greatGrandChild.getId())
                .build();

        when(categoryRepository.findById(greatGrandChild.getId())).thenReturn(Optional.of(greatGrandChild));
        when(categoryMapper.toEntity(any())).thenReturn(new Category());

        ValidationException ex = assertThrows(ValidationException.class, 
                () -> categoryService.createCategory(request));
        
        assertTrue(ex.getMessage().contains("Category hierarchy cannot exceed"));
    }
}
