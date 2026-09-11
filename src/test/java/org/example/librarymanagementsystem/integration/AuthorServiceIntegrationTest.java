package org.example.librarymanagementsystem.integration;

import org.example.librarymanagementsystem.DTO.RequestDTO.AuthorRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.AuthorResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Repository.AuthorRepository;
import org.example.librarymanagementsystem.Service.AuthorService;
import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.exception.AuthorNotFoundException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorServiceIntegrationTest {

    // ─── Real beans, NOT mocks ─────────────────────

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;


    // ─── Test Data ─────────────────────────────────

    private Author author1;
    private Author author2;
    private Author author3;

    private AuthorRequestDTO authorRequestDTO;


    @BeforeEach
    void setUp() {

        System.out.println("─── Setting up Author integration test ───");


        // ─── Author 1 ──────────────────────────────

        author1 = new Author();
        author1.setFirstname("Joshua");
        author1.setLastname("Bloch");
        author1.setEmail("joshua@java.com");
        author1.setNationality("American");
        author1.setBooks(new ArrayList<>());

        authorRepository.save(author1);


        // ─── Author 2 ──────────────────────────────

        author2 = new Author();
        author2.setFirstname("James");
        author2.setLastname("Gosling");
        author2.setEmail("james@java.com");
        author2.setNationality("Canadian");
        author2.setBooks(new ArrayList<>());

        authorRepository.save(author2);


        // ─── Author 3 ──────────────────────────────

        author3 = new Author();
        author3.setFirstname("Robert");
        author3.setLastname("Martin");
        author3.setEmail("robert@cleancode.com");
        author3.setNationality("American");
        author3.setBooks(new ArrayList<>());

        authorRepository.save(author3);


        // ─── Request DTO ───────────────────────────

        authorRequestDTO = new AuthorRequestDTO();

        authorRequestDTO.setFirstname("Martin");
        authorRequestDTO.setLastname("Fowler");
        authorRequestDTO.setEmail("martin@fowler.com");
        authorRequestDTO.setNationality("British");
    }


    // ══════════════════════════════════════════════
    // TEST 1 — save author
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 1: save should persist author to real DB")
    void save_ShouldPersistAuthor_ToRealDatabase() {

        // ACT
        AuthorResponseDTO result =
                authorService.save(authorRequestDTO);

        // ASSERT
        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals(
                "Martin",
                result.getFirstname()
        );

        assertEquals(
                "Fowler",
                result.getLastname()
        );

        assertEquals(
                "martin@fowler.com",
                result.getEmail()
        );

        assertEquals(
                "British",
                result.getNationality()
        );


        // verify database
        assertTrue(
                authorRepository.existsByEmail(
                        "martin@fowler.com"
                )
        );

        System.out.println(
                "✅ Test 1 passed — author saved to real DB"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 2 — getByIdWithBooks
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 2: getByIdWithBooks should retrieve author from real DB")
    void getByIdWithBooks_ShouldRetrieveAuthor_FromRealDatabase() {

        // ACT
        AuthorResponseDTO result =
                authorService.getByIdWithBooks(
                        author1.getId()
                );

        // ASSERT
        assertNotNull(result);

        assertEquals(
                author1.getId(),
                result.getId()
        );

        assertEquals(
                "Joshua",
                result.getFirstname()
        );

        assertEquals(
                "Bloch",
                result.getLastname()
        );

        assertEquals(
                "joshua@java.com",
                result.getEmail()
        );

        assertEquals(
                "American",
                result.getNationality()
        );

        System.out.println(
                "✅ Test 2 passed — author retrieved from DB"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 3 — getById invalid
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 3: getByIdWithBooks should throw AuthorNotFoundException")
    void getByIdWithBooks_ShouldThrow_WhenAuthorNotFound() {

        // ACT + ASSERT
        AuthorNotFoundException exception =
                assertThrows(
                        AuthorNotFoundException.class,
                        () -> authorService.getByIdWithBooks(99999)
                );

        assertNotNull(exception);

        System.out.println(
                "✅ Test 3 passed — AuthorNotFoundException thrown"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 4 — duplicate email
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 4: save should throw DuplicateEmailException")
    void save_ShouldThrow_WhenEmailAlreadyExists() {

        // ACT + ASSERT
        DuplicateEmailException exception =
                assertThrows(
                        DuplicateEmailException.class,
                        () -> authorService.save(
                                new AuthorRequestDTO(
                                        "Another",
                                        "Author",
                                        "joshua@java.com",
                                        "Indian"
                                )
                        )
                );

        assertNotNull(exception);

        System.out.println(
                "✅ Test 4 passed — duplicate email throws exception"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 5 — getAllAuthors
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 5: getAllAuthors should return paginated authors")
    void getAllAuthors_ShouldReturnPagedAuthors() {

        // ACT
        PageResponseDTO<AuthorResponseDTO> result =
                authorService.getAllAuthors(
                        0,
                        10,
                        "firstname",
                        "asc"
                );

        // ASSERT
        assertNotNull(result);

        assertEquals(
                3,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getTotalPages()
        );

        assertEquals(
                3,
                result.getContent().size()
        );

        assertTrue(
                result.isFirst()
        );

        assertTrue(
                result.isLast()
        );

        assertFalse(
                result.isEmpty()
        );

        System.out.println(
                "✅ Test 5 passed — paginated authors returned"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 6 — searchByName
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 6: searchByName should return matching authors")
    void searchByName_ShouldReturnMatchingAuthors() {

        // ACT
        PageResponseDTO<AuthorResponseDTO> result =
                authorService.searchByName(
                        "Joshua",
                        0,
                        10
                );

        // ASSERT
        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getContent().size()
        );

        assertEquals(
                "Joshua",
                result.getContent()
                        .get(0)
                        .getFirstname()
        );

        System.out.println(
                "✅ Test 6 passed — author search works"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 7 — getByNationality
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 7: getByNationality should return matching authors")
    void getByNationality_ShouldReturnAuthors() {

        // ACT
        List<AuthorResponseDTO> result =
                authorService.getByNationality(
                        "American"
                );

        // ASSERT
        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .anyMatch(
                                a -> a.getFirstname()
                                        .equals("Joshua")
                        )
        );

        assertTrue(
                result.stream()
                        .anyMatch(
                                a -> a.getFirstname()
                                        .equals("Robert")
                        )
        );

        System.out.println(
                "✅ Test 7 passed — nationality search works"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 8 — update author
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 8: update should change author fields in real DB")
    void update_ShouldChangeAuthorFields_InRealDatabase() {

        // ARRANGE
        AuthorRequestDTO updateDTO =
                new AuthorRequestDTO();

        updateDTO.setFirstname("Joshua Updated");
        updateDTO.setLastname("Bloch Updated");
        updateDTO.setEmail("joshua.updated@java.com");
        updateDTO.setNationality("American");


        // ACT
        AuthorResponseDTO result =
                authorService.update(
                        author1.getId(),
                        updateDTO
                );


        // ASSERT
        assertNotNull(result);

        assertEquals(
                "Joshua Updated",
                result.getFirstname()
        );

        assertEquals(
                "Bloch Updated",
                result.getLastname()
        );

        assertEquals(
                "joshua.updated@java.com",
                result.getEmail()
        );

        assertEquals(
                "American",
                result.getNationality()
        );


        // verify database
        Author updated =
                authorRepository
                        .findById(author1.getId())
                        .orElse(null);

        assertNotNull(updated);

        assertEquals(
                "Joshua Updated",
                updated.getFirstname()
        );

        System.out.println(
                "✅ Test 8 passed — author updated in DB"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 9 — update invalid ID
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 9: update should throw AuthorNotFoundException")
    void update_ShouldThrow_WhenAuthorNotFound() {

        // ACT + ASSERT
        AuthorNotFoundException exception =
                assertThrows(
                        AuthorNotFoundException.class,
                        () -> authorService.update(
                                99999,
                                authorRequestDTO
                        )
                );

        assertNotNull(exception);

        System.out.println(
                "✅ Test 9 passed — update throws exception"
        );
    }


    // ══════════════════════════════════════════════
    // TEST 10 — delete author
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("Test 10: delete should remove author from real DB")
    void delete_ShouldRemoveAuthor_FromRealDatabase() {

        // ARRANGE
        Integer id = author1.getId();

        assertTrue(
                authorRepository.existsById(id)
        );


        // ACT
        authorService.delete(id);


        // ASSERT
        assertFalse(
                authorRepository.existsById(id)
        );


        // verify service cannot find it
        assertThrows(
                AuthorNotFoundException.class,
                () -> authorService.getByIdWithBooks(id)
        );

        System.out.println(
                "✅ Test 10 passed — author deleted from real DB"
        );
    }
}