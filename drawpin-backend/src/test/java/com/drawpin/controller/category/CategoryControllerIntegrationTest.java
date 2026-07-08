package com.drawpin.controller.category;

import com.drawpin.domain.entity.Category;
import com.drawpin.dto.request.category.CreateCategoryRequest;
import com.drawpin.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_Success() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Digital Art")
                .slug("digital-art")
                .description("Digital artworks")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.slug", is("digital-art")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_AsUser_Forbidden() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Digital Art")
                .slug("digital-art")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRootCategories_Public_Success() throws Exception {
        Category root1 = Category.builder().name("Root 1").slug("root-1").displayOrder(1).active(true).build();
        Category root2 = Category.builder().name("Root 2").slug("root-2").displayOrder(2).active(true).build();
        categoryRepository.save(root1);
        categoryRepository.save(root2);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.length()", is(2)));
    }
}
