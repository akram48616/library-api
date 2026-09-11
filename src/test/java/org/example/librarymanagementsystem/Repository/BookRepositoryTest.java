package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.entity.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("datajpa")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {

        // =========================
        // Book 1
        // =========================
        book1 = Book.builder()
                .title("Effective Java")
                .isbn("9780134685991")
                .price(45.00)
                .stock(10)
                .publishedDate(LocalDate.of(2018, 1, 6))
                .authors(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        // =========================
        // Book 2
        // =========================
        book2 = Book.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .price(40.00)
                .stock(5)
                .publishedDate(LocalDate.of(2008, 8, 1))
                .authors(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        // =========================
        // Book 3
        // =========================
        book3 = Book.builder()
                .title("Java Programming")
                .isbn("9781111111111")
                .price(60.00)
                .stock(0)
                .publishedDate(LocalDate.of(2020, 5, 10))
                .authors(new ArrayList<>())
                .categories(new HashSet<>())
                .build();

        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);

        entityManager.flush();
    }

    // =========================================================
    // TEST 1 - findByIsbn
    // =========================================================

    @Test
    @DisplayName("Test 1: findByIsbn should return book")
    void findByIsbn_ShouldReturnBook() {

        Optional<Book> result =
                bookRepository.findByIsbn("9780134685991");

        assertTrue(result.isPresent());
        assertEquals("Effective Java", result.get().getTitle());
        assertEquals(45.00, result.get().getPrice());
    }

    // =========================================================
    // TEST 2 - existsByIsbn
    // =========================================================

    @Test
    @DisplayName("Test 2: existsByIsbn should return correct result")
    void existsByIsbn_ShouldReturnCorrectResult() {

        assertTrue(
                bookRepository.existsByIsbn("9780134685991")
        );

        assertFalse(
                bookRepository.existsByIsbn("9999999999999")
        );
    }

    // =========================================================
    // TEST 3 - findByTitleContaining
    // =========================================================

    @Test
    @DisplayName("Test 3: findByTitleContaining should find matching books")
    void findByTitleContaining_ShouldFindBooks() {

        List<Book> result =
                bookRepository.findByTitleContaining("Java");

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(book ->
                                book.getTitle().equals("Effective Java"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(book ->
                                book.getTitle().equals("Java Programming"))
        );
    }

    // =========================================================
    // TEST 4 - findByStockGreaterThan
    // =========================================================

    @Test
    @DisplayName("Test 4: findByStockGreaterThan should return available books")
    void findByStockGreaterThan_ShouldReturnAvailableBooks() {

        List<Book> result =
                bookRepository.findByStockGreaterThan(0);

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(book -> book.getStock() > 0)
        );
    }

    // =========================================================
    // TEST 5 - findByPriceBetween
    // =========================================================

    @Test
    @DisplayName("Test 5: findByPriceBetween should return books in price range")
    void findByPriceBetween_ShouldReturnBooksInRange() {

        List<Book> result =
                bookRepository.findByPriceBetween(40.00, 50.00);

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(book ->
                                book.getTitle().equals("Effective Java"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(book ->
                                book.getTitle().equals("Clean Code"))
        );
    }

    // =========================================================
    // TEST 6 - findAll(Pageable)
    // =========================================================

    @Test
    @DisplayName("Test 6: findAll should return paginated books")
    void findAll_ShouldReturnPaginatedBooks() {

        Pageable pageable =
                PageRequest.of(0, 2);

        Page<Book> result =
                bookRepository.findAll(pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());

        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }

    // =========================================================
    // TEST 7 - findByIdWithDetails
    // =========================================================

    @Test
    @DisplayName("Test 7: findByIdWithDetails should return book")
    void findByIdWithDetails_ShouldReturnBook() {

        Optional<Book> result =
                bookRepository.findByIdWithDetails(book1.getId());

        assertTrue(result.isPresent());

        Book found = result.get();

        assertEquals(book1.getId(), found.getId());
        assertEquals("Effective Java", found.getTitle());

        assertNotNull(found.getAuthors());
        assertNotNull(found.getCategories());
    }

    // =========================================================
    // TEST 8 - findAll
    // =========================================================

    @Test
    @DisplayName("Test 8: findAll should return all books")
    void findAll_ShouldReturnAllBooks() {

        List<Book> result =
                bookRepository.findAll();

        assertEquals(3, result.size());
    }

    // =========================================================
    // TEST 9 - decreaseStock
    // =========================================================

    @Test
    @DisplayName("Test 9: decreaseStock should decrease stock")
    void decreaseStock_ShouldDecreaseStock() {

        Integer id = book1.getId();

        int updatedRows =
                bookRepository.decreaseStock(id);

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, updatedRows);

        Book updated =
                bookRepository.findById(id).orElseThrow();

        assertEquals(9, updated.getStock());
    }

    // =========================================================
    // TEST 10 - increaseStock
    // =========================================================

    @Test
    @DisplayName("Test 10: increaseStock should increase stock")
    void increaseStock_ShouldIncreaseStock() {

        Integer id = book1.getId();

        int updatedRows =
                bookRepository.increaseStock(id);

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, updatedRows);

        Book updated =
                bookRepository.findById(id).orElseThrow();

        assertEquals(11, updated.getStock());
    }
}