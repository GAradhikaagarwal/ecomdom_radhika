package com.omnistore.controller;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnistore.dto.CategoryDto;
import com.omnistore.entity.Category;
import com.omnistore.services.CategoryService;

@WebMvcTest(CategoryController.class)
@Import(TestConfig.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic gadgets");

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Electronics");
        categoryDto.setDescription("Electronic gadgets");
    }

    @Test
    void testGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList(category));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(category.getName()));
    }

    @Test
    void testGetCategoryById() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(category.getName()));
    }

    @Test
    void testCreateCategory() throws Exception {
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(category.getName()));
    }
}

@TestConfiguration(proxyBeanMethods = false)
class TestConfig {
    @Bean
    public CategoryService categoryService() {
        return Mockito.mock(CategoryService.class);
    }
}
