package org.example.librarymanagementsystem.integration;

import org.example.librarymanagementsystem.DTO.RequestDTO.BookRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Repository.*;
import org.example.librarymanagementsystem.Service.BookService;
import org.example.librarymanagementsystem.entity.*;
import org.example.librarymanagementsystem.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest                    // ← loads full Spring context
@ActiveProfiles("test")            // ← uses application-test.properties
@Transactional
class BookServiceIntegrationTest {

    // ─── Real beans — NOT mocks! ───────────────────
    @Autowired
    private BookService bookService;        // real service

    @Autowired
    private BookRepository bookRepository;  // real repository

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    // ─── Test Data ─────────────────────────────────
    private Publisher publisher;
    private Author author;
    private Category category;
    private BookRequestDTO bookRequestDTO;

    @BeforeEach
    void setUp() {
        System.out.println("─── Setting up integration test ───");

        // save real data to real test DB
        publisher = new Publisher();
        publisher.setName("Oxford Press");
        publisher.setAddress("Oxford Street");
        publisher.setPhone("9876543210");
        publisherRepository.save(publisher);

        author = new Author();
        author.setFirstname("Joshua");
        author.setLastname("Bloch");
        author.setEmail("joshua@java.com");
        author.setNationality("American");
        authorRepository.save(author);

        category = new Category();
        category.setName("Programming");
        category.setDescription("Programming books");
        categoryRepository.save(category);

        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Effective Java");
        bookRequestDTO.setIsbn("9780134685991");
        bookRequestDTO.setPrice(599.0);
        bookRequestDTO.setStock(10);
        bookRequestDTO.setPublishedDate(LocalDate.of(2018, 1, 6));
        bookRequestDTO.setPublisherId(publisher.getId());
        bookRequestDTO.setAuthorIds(List.of(author.getId()));
        bookRequestDTO.setCategoryIds(List.of(category.getId()));
    }

    // ══════════════════════════════════════════════
    // TEST 1 — save book to real DB and retrieve it
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 1: save should persist book to real DB")
    void save_ShouldPersistBook_ToRealDatabase() {

        // ACT — calls real service → real repository → real DB
        BookResponseDTO result = bookService.save(bookRequestDTO);

        // ASSERT
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Effective Java", result.getTitle());
        assertEquals("9780134685991", result.getIsbn());
        assertTrue(result.isAvailable());

        // verify it's actually in DB
        assertTrue(bookRepository
                .existsByIsbn("9780134685991"));

        System.out.println("✅ Test 1 passed — book saved to real DB");
    }

    // ══════════════════════════════════════════════
    // TEST 2 — getById retrieves from real DB
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 2: getById should retrieve book from real DB")
    void getById_ShouldRetrieveBook_FromRealDatabase() {

        // ARRANGE — save first
        BookResponseDTO saved = bookService.save(bookRequestDTO);

        // ACT — retrieve by id
        BookResponseDTO result = bookService.getById(saved.getId());

        // ASSERT
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Effective Java", result.getTitle());
        assertNotNull(result.getPublisher());
        assertFalse(result.getAuthors().isEmpty());
        assertFalse(result.getCategories().isEmpty());

        System.out.println("✅ Test 2 passed — book retrieved from DB");
    }

    // ══════════════════════════════════════════════
    // TEST 3 — getById throws when not found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 3: getById should throw BookNotFoundException")
    void getById_ShouldThrow_WhenBookNotFound() {

        // ACT + ASSERT
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.getById(99999)
        );

        assertEquals(
                "Book with id 99999 not found",
                exception.getMessage()
        );

        System.out.println("✅ Test 3 passed — throws exception");
    }

    // ══════════════════════════════════════════════
    // TEST 4 — save throws for duplicate isbn
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 4: save should throw DuplicateIsbnException")
    void save_ShouldThrow_WhenIsbnAlreadyExists() {

        // ARRANGE — save once
        bookService.save(bookRequestDTO);

        // ACT + ASSERT — save again with same isbn
        DuplicateIsbnException exception = assertThrows(
                DuplicateIsbnException.class,
                () -> bookService.save(bookRequestDTO)
        );

        assertEquals(
                "Book with ISBN 9780134685991 already exists",
                exception.getMessage()
        );

        System.out.println("✅ Test 4 passed — duplicate isbn throws");
    }

    // ══════════════════════════════════════════════
    // TEST 5 — update book in real DB
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 5: update should change book fields in real DB")
    void update_ShouldChangeBookFields_InRealDatabase() {

        // ARRANGE — save first
        BookResponseDTO saved = bookService.save(bookRequestDTO);

        // create update request
        BookRequestDTO updateDTO = new BookRequestDTO();
        updateDTO.setTitle("Effective Java 3rd Edition");
        updateDTO.setIsbn("9780134685991");  // same isbn
        updateDTO.setPrice(799.0);           // new price
        updateDTO.setStock(20);              // new stock
        updateDTO.setPublishedDate(LocalDate.of(2023, 1, 1));
        updateDTO.setPublisherId(publisher.getId());
        updateDTO.setAuthorIds(List.of(author.getId()));
        updateDTO.setCategoryIds(List.of(category.getId()));

        // ACT
        BookResponseDTO updated =
                bookService.update(saved.getId(), updateDTO);

        // ASSERT
        assertNotNull(updated);
        assertEquals("Effective Java 3rd Edition",
                updated.getTitle());
        assertEquals(799.0, updated.getPrice());
        assertEquals(20, updated.getStock());

        System.out.println("✅ Test 5 passed — book updated in DB");
    }

    // ══════════════════════════════════════════════
    // TEST 6 — delete book from real DB
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 6: delete should remove book from real DB")
    void delete_ShouldRemoveBook_FromRealDatabase() {

        // ARRANGE — save first
        BookResponseDTO saved = bookService.save(bookRequestDTO);
        Integer savedId = saved.getId();

        // ACT
        bookService.delete(savedId);

        // ASSERT — book should no longer exist
        assertThrows(
                BookNotFoundException.class,
                () -> bookService.getById(savedId)
        );

        System.out.println("✅ Test 6 passed — book deleted from DB");
    }

    // ══════════════════════════════════════════════
    // TEST 7 — getAllBooks returns paged list
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 7: getAllBooks should return paged list from DB")
    void getAllBooks_ShouldReturnPagedList() {

        // ARRANGE — save 2 books
        bookService.save(bookRequestDTO);

        BookRequestDTO book2DTO = new BookRequestDTO();
        book2DTO.setTitle("Clean Code");
        book2DTO.setIsbn("9780132350884");
        book2DTO.setPrice(499.0);
        book2DTO.setStock(5);
        book2DTO.setPublishedDate(LocalDate.of(2023, 1, 1));
        book2DTO.setPublisherId(publisher.getId());
        book2DTO.setAuthorIds(List.of(author.getId()));
        bookService.save(book2DTO);

        // ACT
        PageResponseDTO<BookResponseDTO> result =
                bookService.getAllBooks(0, 10, "title", "asc");

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertFalse(result.isEmpty());
        assertFalse(result.getContent().isEmpty());

        System.out.println("✅ Test 7 passed — paged list returned");
    }

    // ══════════════════════════════════════════════
    // TEST 8 — searchByTitle returns matching books
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 8: searchByTitle should return matching books")
    void searchByTitle_ShouldReturnMatchingBooks() {

        // ARRANGE
        bookService.save(bookRequestDTO);

        // ACT
        PageResponseDTO<BookResponseDTO> result =
                bookService.searchByTitle("Effective", 0, 10);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Effective Java",
                result.getContent().get(0).getTitle());

        System.out.println("✅ Test 8 passed — search works");
    }

    // ══════════════════════════════════════════════
    // TEST 9 — getAvailableBooks returns stock > 0
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 9: getAvailableBooks should return books with stock > 0")
    void getAvailableBooks_ShouldReturnOnlyAvailable() {

        // ARRANGE — save available book
        bookService.save(bookRequestDTO);

        // save out of stock book
        BookRequestDTO outOfStock = new BookRequestDTO();
        outOfStock.setTitle("Out of Stock Book");
        outOfStock.setIsbn("9780000000001");
        outOfStock.setPrice(299.0);
        outOfStock.setPublishedDate(LocalDate.of(2020, 1, 1));
        outOfStock.setStock(0);          // ← out of stock!
        outOfStock.setPublisherId(publisher.getId());
        outOfStock.setAuthorIds(List.of(author.getId()));
        bookService.save(outOfStock);

        // ACT
        PageResponseDTO<BookResponseDTO> result =
                bookService.getAvailableBooks(0, 10);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        // only "Effective Java" should be returned
        assertEquals("Effective Java",
                result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).isAvailable());

        System.out.println("✅ Test 9 passed — available books only");
    }

    // ══════════════════════════════════════════════
    // TEST 10 — getByCategory returns correct books
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 10: getByCategory should return books in category")
    void getByCategory_ShouldReturnBooksInCategory() {

        // ARRANGE
        bookService.save(bookRequestDTO);

        // ACT
        List<BookResponseDTO> result =
                bookService.getByCategory(category.getId());

        // ASSERT
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Effective Java",
                result.get(0).getTitle());

        System.out.println("✅ Test 10 passed — books by category");
    }
}