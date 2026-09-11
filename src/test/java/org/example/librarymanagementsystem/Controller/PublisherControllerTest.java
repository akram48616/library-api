package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.DTO.RequestDTO.PublisherRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PublisherResponseDTO;
import org.example.librarymanagementsystem.Service.PublisherService;
import org.example.librarymanagementsystem.exception.PublisherNotFoundException;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublisherController.class)
@ActiveProfiles("webmvc")
class PublisherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublisherService publisherService;

    @Autowired
    private ObjectMapper objectMapper;

    private PublisherRequestDTO requestDTO;
    private PublisherResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        requestDTO = PublisherRequestDTO.builder()
                .name("Oxford Press")
                .address("London")
                .phone("9876543210")
                .build();

        responseDTO = PublisherResponseDTO.builder()
                .id(1)
                .name("Oxford Press")
                .address("London")
                .phone("9876543210")
                .build();
    }

    // ====================================================
    // GET ALL
    // ====================================================

    @Test
    @DisplayName("GET /api/publishers should return all publishers")
    void getAll_ShouldReturn200() throws Exception {

        when(publisherService.getAllPublishers())
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/publishers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Oxford Press"))
                .andExpect(jsonPath("$[0].phone")
                        .value("9876543210"));

        verify(publisherService).getAllPublishers();
    }

    // ====================================================
    // GET BY ID
    // ====================================================

    @Test
    @DisplayName("GET /api/publishers/{id}")
    void getById_ShouldReturn200() throws Exception {

        when(publisherService.getById(1))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/publishers/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Oxford Press"))
                .andExpect(jsonPath("$.address")
                        .value("London"));

        verify(publisherService).getById(1);
    }

    // ====================================================
    // GET BY ID NOT FOUND
    // ====================================================

    @Test
    @DisplayName("GET /api/publishers/{id} should return 404")
    void getById_ShouldReturn404() throws Exception {

        when(publisherService.getById(999))
                .thenThrow(new PublisherNotFoundException(999));

        mockMvc.perform(get("/api/publishers/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Publisher with id 999 not found"));

        verify(publisherService).getById(999);
    }

    // ====================================================
    // GET WITH BOOKS
    // ====================================================

    @Test
    @DisplayName("GET /api/publishers/{id}/books")
    void getWithBooks_ShouldReturn200() throws Exception {

        when(publisherService.getByIdWithBooks(1))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/publishers/1/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Oxford Press"));

        verify(publisherService).getByIdWithBooks(1);
    }

    // ====================================================
    // SEARCH
    // ====================================================

    @Test
    @DisplayName("GET /api/publishers/search")
    void search_ShouldReturn200() throws Exception {

        when(publisherService.searchByName("Oxford"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/publishers/search")
                        .param("keyword", "Oxford"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Oxford Press"));

        verify(publisherService).searchByName("Oxford");
    }
    // ====================================================
    // POST SUCCESS
    // ====================================================

    @Test
    @DisplayName("POST /api/publishers should create publisher")
    void save_ShouldReturn200() throws Exception {

        when(publisherService.save(any(PublisherRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Oxford Press"))
                .andExpect(jsonPath("$.address").value("London"))
                .andExpect(jsonPath("$.phone").value("9876543210"));

        verify(publisherService, times(1))
                .save(any(PublisherRequestDTO.class));
    }

    // ====================================================
    // POST VALIDATION ERROR
    // ====================================================

    @Test
    @DisplayName("POST /api/publishers should return 400 for invalid data")
    void save_ShouldReturn400_WhenValidationFails() throws Exception {

        PublisherRequestDTO invalid = new PublisherRequestDTO();
        invalid.setName("");
        invalid.setAddress("");
        invalid.setPhone("123");

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));

        verify(publisherService, never())
                .save(any(PublisherRequestDTO.class));
    }

    // ====================================================
    // PUT SUCCESS
    // ====================================================

    @Test
    @DisplayName("PUT /api/publishers/{id} should update publisher")
    void update_ShouldReturn200() throws Exception {

        when(publisherService.update(eq(1),
                any(PublisherRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/publishers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Oxford Press"))
                .andExpect(jsonPath("$.address")
                        .value("London"));

        verify(publisherService, times(1))
                .update(eq(1), any(PublisherRequestDTO.class));
    }

    // ====================================================
    // PUT NOT FOUND
    // ====================================================

    @Test
    @DisplayName("PUT /api/publishers/{id} should return 404")
    void update_ShouldReturn404() throws Exception {

        when(publisherService.update(eq(999),
                any(PublisherRequestDTO.class)))
                .thenThrow(new PublisherNotFoundException(999));

        mockMvc.perform(put("/api/publishers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Publisher with id 999 not found"));

        verify(publisherService, times(1))
                .update(eq(999), any(PublisherRequestDTO.class));
    }
    // ════════════════════════════════════════════════
    // TEST 8 — DELETE publisher success
    // ════════════════════════════════════════════════
    @Test
    @DisplayName("DELETE /api/publishers/{id} should return 200")
    void delete_ShouldReturn200_WhenDeleted() throws Exception {

        doNothing().when(publisherService).delete(1);

        mockMvc.perform(delete("/api/publishers/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Publisher deleted successfully"));

        verify(publisherService, times(1)).delete(1);
    }

    // ════════════════════════════════════════════════
    // TEST 9 — DELETE publisher not found
    // ════════════════════════════════════════════════
    @Test
    @DisplayName("DELETE /api/publishers/{id} should return 404")
    void delete_ShouldReturn404_WhenPublisherNotFound() throws Exception {

        doThrow(new PublisherNotFoundException(999))
                .when(publisherService).delete(999);

        mockMvc.perform(delete("/api/publishers/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Publisher with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/publishers/999"));

        verify(publisherService, times(1)).delete(999);
    }

}