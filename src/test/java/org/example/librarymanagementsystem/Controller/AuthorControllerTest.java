package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.DTO.RequestDTO.AuthorRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.AuthorResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.BookResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Service.AuthorService;
import org.example.librarymanagementsystem.exception.AuthorNotFoundException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
@ActiveProfiles("webmvc")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService service;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthorRequestDTO requestDTO;
    private AuthorResponseDTO responseDTO;
    private PageResponseDTO<AuthorResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {

        BookResponseDTO book = new BookResponseDTO();
        book.setId(1);
        book.setTitle("Effective Java");
        book.setPublishedDate(LocalDate.of(2018,1,6));

        responseDTO = new AuthorResponseDTO();
        responseDTO.setId(1);
        responseDTO.setFirstname("Joshua");
        responseDTO.setLastname("Bloch");
        responseDTO.setEmail("joshua@java.com");
        responseDTO.setNationality("American");


        requestDTO = new AuthorRequestDTO();
        requestDTO.setFirstname("Joshua");
        requestDTO.setLastname("Bloch");
        requestDTO.setEmail("joshua@java.com");
        requestDTO.setNationality("American");

        pageResponse = PageResponseDTO.<AuthorResponseDTO>builder()
                .content(List.of(responseDTO))
                .currentPage(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1L)
                .first(true)
                .last(true)
                .empty(false)
                .build();
    }

    @Test
    void getAll_ShouldReturn200() throws Exception {

        when(service.getAllAuthors(0,10,"firstname","asc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstname").value("Joshua"));

        verify(service).getAllAuthors(0,10,"firstname","asc");
    }

    @Test
    void getById_ShouldReturn200() throws Exception {

        when(service.getByIdWithBooks(1))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Joshua"))
                .andExpect(jsonPath("$.lastname").value("Bloch"))
                .andExpect(jsonPath("$.email").value("joshua@java.com"))
                .andExpect(jsonPath("$.nationality").value("American"));

        verify(service).getByIdWithBooks(1);
    }

    @Test
    void getById_ShouldReturn404() throws Exception {

        when(service.getByIdWithBooks(999))
                .thenThrow(new AuthorNotFoundException(999));

        mockMvc.perform(get("/api/authors/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(service).getByIdWithBooks(999);
    }

    @Test
    void search_ShouldReturn200() throws Exception {

        when(service.searchByName("Joshua",0,10))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/authors/search")
                        .param("keyword","Joshua"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)));

        verify(service).searchByName("Joshua",0,10);
    }

    @Test
    void nationality_ShouldReturn200() throws Exception {

        when(service.getByNationality("American"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/authors/nationality/American"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)));

        verify(service).getByNationality("American");
    }

    @Test
    void save_ShouldReturn200() throws Exception {

        when(service.save(any(AuthorRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Joshua"));

        verify(service).save(any(AuthorRequestDTO.class));
    }

    @Test
    void save_ShouldReturn409() throws Exception {

        when(service.save(any(AuthorRequestDTO.class)))
                .thenThrow(new DuplicateEmailException("joshua@java.com"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(service).save(any(AuthorRequestDTO.class));
    }

    @Test
    void save_ShouldReturn400() throws Exception {

        AuthorRequestDTO dto = new AuthorRequestDTO();
        dto.setFirstname("");

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_ShouldReturn200() throws Exception {

        when(service.update(eq(1),any(AuthorRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Joshua"));

        verify(service).update(eq(1),any(AuthorRequestDTO.class));
    }

    @Test
    void update_ShouldReturn404() throws Exception {

        when(service.update(eq(999),any(AuthorRequestDTO.class)))
                .thenThrow(new AuthorNotFoundException(999));

        mockMvc.perform(put("/api/authors/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(service).update(eq(999),any(AuthorRequestDTO.class));
    }

    @Test
    void delete_ShouldReturn200() throws Exception {

        doNothing().when(service).delete(1);

        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Author deleted successfully"));

        verify(service).delete(1);
    }

}