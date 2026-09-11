package org.example.librarymanagementsystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagementsystem.Controller.BookController;
import org.example.librarymanagementsystem.DTO.RequestDTO.BookRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.BookService;
import org.example.librarymanagementsystem.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@ActiveProfiles("webmvc")// ← load only BookController
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;        // ← auto-configured

    @MockitoBean
    private BookService bookService; // ← mock service

    @Autowired
    private ObjectMapper objectMapper; // ← JSON converter

    // ─── Test Data ─────────────────────────────────
    private BookResponseDTO bookResponseDTO;
    private BookRequestDTO bookRequestDTO;
    private PageResponseDTO<BookResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {

        // publisher response
        PublisherResponseDTO publisher =
                new PublisherResponseDTO();
        publisher.setId(1);
        publisher.setName("Oxford Press");
        publisher.setPhone("9876543210");

        // author response
        AuthorResponseDTO author = new AuthorResponseDTO();
        author.setId(1);
        author.setFirstname("Joshua");
        author.setLastname("Bloch");
        author.setEmail("joshua@java.com");

        // category response
        CategoryResponseDTO category =
                new CategoryResponseDTO();
        category.setId(1);
        category.setName("Programming");

        // book response DTO
        bookResponseDTO = new BookResponseDTO();
        bookResponseDTO.setId(1);
        bookResponseDTO.setTitle("Effective Java");
        bookResponseDTO.setIsbn("9780134685991");
        bookResponseDTO.setPrice(599.0);
        bookResponseDTO.setStock(10);
        bookResponseDTO.setAvailable(true);
        bookResponseDTO.setPublishedDate(
                LocalDate.of(2018, 1, 6));
        bookResponseDTO.setPublisher(publisher);
        bookResponseDTO.setAuthors(List.of(author));
        bookResponseDTO.setCategories(List.of(category));

        // book request DTO
        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Effective Java");
        bookRequestDTO.setIsbn("9780134685991");
        bookRequestDTO.setPrice(599.0);
        bookRequestDTO.setStock(10);
        bookRequestDTO.setPublishedDate(
                LocalDate.of(2018, 1, 6));
        bookRequestDTO.setPublisherId(1);
        bookRequestDTO.setAuthorIds(List.of(1));
        bookRequestDTO.setCategoryIds(List.of(1));

        // page response
        pageResponse = PageResponseDTO
                .<BookResponseDTO>builder()
                .content(List.of(bookResponseDTO))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .first(true)
                .last(true)
                .empty(false)
                .build();
    }

    // ══════════════════════════════════════════════
    // TEST 1 — GET /api/books
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("GET /api/books should return 200 with paged list")
    void getAllBooks_ShouldReturn200_WithPagedList()
            throws Exception {

        // ARRANGE
        when(bookService.getAllBooks(0, 10, "title", "asc"))
                .thenReturn(pageResponse);

        // ACT + ASSERT
        mockMvc.perform(
                        get("/api/books")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "title")
                                .param("direction", "asc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.content[0].isbn")
                        .value("9780134685991"))
                .andExpect(jsonPath("$.content[0].price")
                        .value(599.0))
                .andExpect(jsonPath("$.content[0].available")
                        .value(true));

        verify(bookService, times(1))
                .getAllBooks(0, 10, "title", "asc");
    }

    // ══════════════════════════════════════════════
    // TEST 2 — GET /api/books/{id} found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("GET /api/books/{id} should return 200 with book")
    void getById_ShouldReturn200_WithBook()
            throws Exception {

        // ARRANGE
        when(bookService.getById(1))
                .thenReturn(bookResponseDTO);

        // ACT + ASSERT
        mockMvc.perform(get("/api/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.isbn")
                        .value("9780134685991"))
                .andExpect(jsonPath("$.price").value(599.0))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.publisher.name")
                        .value("Oxford Press"))
                .andExpect(jsonPath("$.authors[0].firstname")
                        .value("Joshua"))
                .andExpect(jsonPath("$.categories[0].name")
                        .value("Programming"));
    }

    // ══════════════════════════════════════════════
    // TEST 3 — GET /api/books/{id} not found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("GET /api/books/{id} should return 404 when not found")
    void getById_ShouldReturn404_WhenNotFound()
            throws Exception {

        // ARRANGE
        when(bookService.getById(999))
                .thenThrow(new BookNotFoundException(999));

        // ACT + ASSERT
        mockMvc.perform(get("/api/books/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Book with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/books/999"))
                .andExpect(jsonPath("$.timestamp")
                        .isNotEmpty());
    }

    // ══════════════════════════════════════════════
    // TEST 4 — POST /api/books success
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("POST /api/books should return 200 when created")
    void save_ShouldReturn200_WhenBookCreated()
            throws Exception {

        // ARRANGE
        when(bookService.save(any(BookRequestDTO.class)))
                .thenReturn(bookResponseDTO);

        // ACT + ASSERT
        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(bookRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.isbn")
                        .value("9780134685991"));

        verify(bookService, times(1))
                .save(any(BookRequestDTO.class));
    }

    // ══════════════════════════════════════════════
    // TEST 5 — POST /api/books duplicate isbn
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("POST /api/books should return 409 for duplicate isbn")
    void save_ShouldReturn409_WhenIsbnDuplicate()
            throws Exception {

        // ARRANGE
        when(bookService.save(any(BookRequestDTO.class)))
                .thenThrow(new DuplicateIsbnException(
                        "9780134685991"));

        // ACT + ASSERT
        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(bookRequestDTO)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Book with ISBN 9780134685991 already exists"));

        verify(bookService, times(1))
                .save(any(BookRequestDTO.class));
    }

    // ══════════════════════════════════════════════
    // TEST 6 — POST /api/books validation error
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("POST /api/books should return 400 for invalid data")
    void save_ShouldReturn400_WhenValidationFails() throws Exception {

        BookRequestDTO invalid = new BookRequestDTO();
        invalid.setTitle("");
        invalid.setIsbn("");
        invalid.setPrice(-100.0);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verifyNoInteractions(bookService);
    }

    // ══════════════════════════════════════════════
    // TEST 7 — PUT /api/books/{id} success
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("PUT /api/books/{id} should return 200 when updated")
    void update_ShouldReturn200_WhenUpdated()
            throws Exception {

        // ARRANGE
        when(bookService.update(
                eq(1), any(BookRequestDTO.class)))
                .thenReturn(bookResponseDTO);

        // ACT + ASSERT
        mockMvc.perform(
                        put("/api/books/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(bookRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Effective Java"));

        verify(bookService, times(1))
                .update(eq(1), any(BookRequestDTO.class));
    }

    // ══════════════════════════════════════════════
    // TEST 8 — PUT /api/books/{id} not found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("PUT /api/books/{id} should return 404 when not found")
    void update_ShouldReturn404_WhenNotFound()
            throws Exception {

        // ARRANGE
        when(bookService.update(
                eq(999), any(BookRequestDTO.class)))
                .thenThrow(new BookNotFoundException(999));

        // ACT + ASSERT
        mockMvc.perform(
                        put("/api/books/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                        .writeValueAsString(bookRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Book with id 999 not found"));
    }

    // ══════════════════════════════════════════════
    // TEST 9 — DELETE /api/books/{id} success
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("DELETE /api/books/{id} should return 200")
    void delete_ShouldReturn200_WhenDeleted()
            throws Exception {

        // ARRANGE
        doNothing().when(bookService).delete(1);

        // ACT + ASSERT
        mockMvc.perform(delete("/api/books/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Book deleted successfully"));

        verify(bookService, times(1)).delete(1);
    }

    // ══════════════════════════════════════════════
    // TEST 10 — GET /api/books/search
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("GET /api/books/search should return matching books")
    void search_ShouldReturn200_WithResults()
            throws Exception {

        // ARRANGE
        when(bookService.searchByTitle("java", 0, 10))
                .thenReturn(pageResponse);

        // ACT + ASSERT
        mockMvc.perform(
                        get("/api/books/search")
                                .param("keyword", "java")
                                .param("page", "0")
                                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Effective Java"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(bookService, times(1))
                .searchByTitle("java", 0, 10);
    }
}