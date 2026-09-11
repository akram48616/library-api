package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.DTO.RequestDTO.CategoryRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.librarymanagementsystem.Service.CategoryService;
import org.example.librarymanagementsystem.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("webmvc")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDTO requestDTO;
    private CategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        requestDTO = CategoryRequestDTO.builder()
                .name("Programming")
                .description("Programming Books")
                .build();

        responseDTO = CategoryResponseDTO.builder()
                .id(1)
                .name("Programming")
                .description("Programming Books")
                .build();
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    @DisplayName("GET /api/categories should return all categories")
    void getAll_ShouldReturn200() throws Exception {

        when(categoryService.getAllCategories())
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Programming"))
                .andExpect(jsonPath("$[0].description")
                        .value("Programming Books"));

        verify(categoryService, times(1))
                .getAllCategories();
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    @DisplayName("GET /api/categories/{id} should return category")
    void getById_ShouldReturn200() throws Exception {

        when(categoryService.getByid(1))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/categories/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Programming"))
                .andExpect(jsonPath("$.description")
                        .value("Programming Books"));

        verify(categoryService).getByid(1);
    }

    // ============================================================
    // GET BY ID NOT FOUND
    // ============================================================

    @Test
    @DisplayName("GET /api/categories/{id} should return 404")
    void getById_ShouldReturn404() throws Exception {

        when(categoryService.getByid(999))
                .thenThrow(new CategoryNotFoundException(999));

        mockMvc.perform(get("/api/categories/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Category with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/categories/999"));

        verify(categoryService).getByid(999);
    }

    // ============================================================
    // GET WITH BOOKS
    // ============================================================

    @Test
    @DisplayName("GET /api/categories/{id}/books")
    void getWithBooks_ShouldReturn200() throws Exception {

        when(categoryService.getByid(1))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/categories/1/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Programming"));

        verify(categoryService).getByid(1);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    @DisplayName("GET /api/categories/search")
    void search_ShouldReturn200() throws Exception {

        when(categoryService.searchByName("Programming"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", "Programming"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Programming"));

        verify(categoryService)
                .searchByName("Programming");
    }
    // ============================================================
    // POST SUCCESS
    // ============================================================

    @Test
    @DisplayName("POST /api/categories should create category")
    void save_ShouldReturn200() throws Exception {

        when(categoryService.save(any(CategoryRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Programming"))
                .andExpect(jsonPath("$.description")
                        .value("Programming Books"));

        verify(categoryService, times(1))
                .save(any(CategoryRequestDTO.class));
    }

    // ============================================================
    // POST VALIDATION ERROR
    // ============================================================

    @Test
    @DisplayName("POST /api/categories should return 400 for invalid data")
    void save_ShouldReturn400_WhenValidationFails() throws Exception {

        CategoryRequestDTO invalid = new CategoryRequestDTO();
        invalid.setName("");
        invalid.setDescription("Programming");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));

        verify(categoryService, never())
                .save(any(CategoryRequestDTO.class));
    }

    // ============================================================
    // PUT SUCCESS
    // ============================================================

    @Test
    @DisplayName("PUT /api/categories/{id} should update category")
    void update_ShouldReturn200() throws Exception {

        when(categoryService.update(eq(1), any(CategoryRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Programming"));

        verify(categoryService, times(1))
                .update(eq(1), any(CategoryRequestDTO.class));
    }

    // ============================================================
    // PUT NOT FOUND
    // ============================================================

    @Test
    @DisplayName("PUT /api/categories/{id} should return 404")
    void update_ShouldReturn404() throws Exception {

        when(categoryService.update(eq(999), any(CategoryRequestDTO.class)))
                .thenThrow(new CategoryNotFoundException(999));

        mockMvc.perform(put("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Category with id 999 not found"));

        verify(categoryService)
                .update(eq(999), any(CategoryRequestDTO.class));
    }
    // ============================================================
    // DELETE SUCCESS
    // ============================================================

    @Test
    @DisplayName("DELETE /api/categories/{id} should delete category")
    void delete_ShouldReturn200() throws Exception {

        doNothing().when(categoryService).delete(1);

        mockMvc.perform(delete("/api/categories/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Category deleted successfully"));

        verify(categoryService, times(1)).delete(1);
    }

    // ============================================================
    // DELETE NOT FOUND
    // ============================================================

    @Test
    @DisplayName("DELETE /api/categories/{id} should return 404 when category not found")
    void delete_ShouldReturn404() throws Exception {

        doThrow(new CategoryNotFoundException(999))
                .when(categoryService)
                .delete(999);

        mockMvc.perform(delete("/api/categories/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Category with id 999 not found"));

        verify(categoryService, times(1)).delete(999);
    }

}
