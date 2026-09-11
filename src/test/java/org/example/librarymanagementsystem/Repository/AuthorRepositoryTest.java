package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("datajpa")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    private Author author1;
    private Author author2;
    private Author author3;

    @BeforeEach
    void setUp() {

        author1 = new Author();
        author1.setFirstname("Joshua");
        author1.setLastname("Bloch");
        author1.setEmail("joshua@java.com");
        author1.setNationality("American");
        author1.setBooks(new ArrayList<>());

        author2 = new Author();
        author2.setFirstname("James");
        author2.setLastname("Gosling");
        author2.setEmail("james@java.com");
        author2.setNationality("Canadian");
        author2.setBooks(new ArrayList<>());

        author3 = new Author();
        author3.setFirstname("Robert");
        author3.setLastname("Martin");
        author3.setEmail("robert@cleancode.com");
        author3.setNationality("American");
        author3.setBooks(new ArrayList<>());

        entityManager.persist(author1);
        entityManager.persist(author2);
        entityManager.persist(author3);

        entityManager.flush();
        entityManager.clear();
    }

    // =====================================================
    // findByEmail()
    // =====================================================

    @Test
    @DisplayName("findByEmail should return author when email exists")
    void findByEmail_ShouldReturnAuthor_WhenEmailExists() {

        Author result =
                authorRepository.findByEmail("joshua@java.com");

        assertNotNull(result);
        assertEquals("Joshua", result.getFirstname());
        assertEquals("Bloch", result.getLastname());
        assertEquals("American", result.getNationality());
    }

    @Test
    @DisplayName("findByEmail should return null when email does not exist")
    void findByEmail_ShouldReturnNull_WhenEmailNotFound() {

        Author result =
                authorRepository.findByEmail("notfound@test.com");

        assertNull(result);
    }

    // =====================================================
    // existsByEmail()
    // =====================================================

    @Test
    @DisplayName("existsByEmail should return correct boolean")
    void existsByEmail_ShouldReturnCorrectBoolean() {

        assertTrue(
                authorRepository.existsByEmail("joshua@java.com")
        );

        assertFalse(
                authorRepository.existsByEmail("none@test.com")
        );
    }

    // =====================================================
    // findByNationality()
    // =====================================================

    @Test
    @DisplayName("findByNationality should return authors by nationality")
    void findByNationality_ShouldReturnAuthors() {

        List<Author> americans =
                authorRepository.findByNationality("American");

        assertEquals(2, americans.size());

        List<Author> canadians =
                authorRepository.findByNationality("Canadian");

        assertEquals(1, canadians.size());
        assertEquals("James", canadians.get(0).getFirstname());

        List<Author> indians =
                authorRepository.findByNationality("Indian");

        assertTrue(indians.isEmpty());
    }

    // =====================================================
    // search by firstname / lastname
    // =====================================================

    @Test
    @DisplayName("search by name should return matching authors")
    void searchByName_ShouldReturnMatchingAuthors() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Author> jamesResult =
                authorRepository.findByFirstnameContainsOrLastnameContains(
                        "James",
                        "James",
                        pageable
                );

        assertEquals(1, jamesResult.getTotalElements());
        assertEquals(
                "James",
                jamesResult.getContent().get(0).getFirstname()
        );

        Page<Author> blochResult =
                authorRepository.findByFirstnameContainsOrLastnameContains(
                        "Bloch",
                        "Bloch",
                        pageable
                );

        assertEquals(1, blochResult.getTotalElements());
        assertEquals(
                "Joshua",
                blochResult.getContent().get(0).getFirstname()
        );

        Page<Author> emptyResult =
                authorRepository.findByFirstnameContainsOrLastnameContains(
                        "xyz",
                        "xyz",
                        pageable
                );

        assertEquals(0, emptyResult.getTotalElements());
    }

    // =====================================================
    // findAll(Pageable)
    // =====================================================

    @Test
    @DisplayName("findAll paged should return correct page")
    void findAll_Paged_ShouldReturnCorrectPage() {

        Pageable pageable =
                PageRequest.of(
                        0,
                        2,
                        Sort.by("firstname").ascending()
                );

        Page<Author> page =
                authorRepository.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(2, page.getContent().size());

        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    // =====================================================
    // findByIdWithBooks()
    // =====================================================

    @Test
    @DisplayName("findByIdWithBooks should return author with books")
    void findByIdWithBooks_ShouldReturnAuthorWithBooks() {

        Author result =
                authorRepository.findByIdWithBooks(author1.getId());

        assertNotNull(result);
        assertEquals("Joshua", result.getFirstname());
        assertNotNull(result.getBooks());
    }

    @Test
    @DisplayName("findByIdWithBooks should return null for invalid id")
    void findByIdWithBooks_ShouldReturnNull_WhenIdDoesNotExist() {

        Author result =
                authorRepository.findByIdWithBooks(999);

        assertNull(result);
    }

    // =====================================================
    // save()
    // =====================================================

    @Test
    @DisplayName("save should persist author")
    void save_ShouldPersistAuthor() {

        Author newAuthor = new Author();

        newAuthor.setFirstname("Martin");
        newAuthor.setLastname("Fowler");
        newAuthor.setEmail("martin@fowler.com");
        newAuthor.setNationality("British");
        newAuthor.setBooks(new ArrayList<>());

        Author saved =
                authorRepository.save(newAuthor);

        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("Martin", saved.getFirstname());
        assertEquals(4, authorRepository.count());
    }

    // =====================================================
    // deleteById()
    // =====================================================

    @Test
    @DisplayName("deleteById should remove author")
    void deleteById_ShouldRemoveAuthor() {

        Integer id = author1.getId();

        authorRepository.deleteById(id);

        entityManager.flush();
        entityManager.clear();

        assertFalse(
                authorRepository.existsById(id)
        );

        assertEquals(
                2,
                authorRepository.count()
        );
    }

    // =====================================================
    // findAll()
    // =====================================================

    @Test
    @DisplayName("findAll should return all authors")
    void findAll_ShouldReturnAllAuthors() {

        List<Author> authors =
                authorRepository.findAll();

        assertEquals(3, authors.size());
    }

    // =====================================================
    // findById()
    // =====================================================

    @Test
    @DisplayName("findById should return author")
    void findById_ShouldReturnAuthor() {

        Author result =
                authorRepository
                        .findById(author1.getId())
                        .orElse(null);

        assertNotNull(result);
        assertEquals("Joshua", result.getFirstname());
        assertEquals("Bloch", result.getLastname());
    }

    @Test
    @DisplayName("findById should return empty for invalid id")
    void findById_ShouldReturnEmpty() {

        assertFalse(
                authorRepository.findById(999).isPresent()
        );
    }

    // =====================================================
    // count()
    // =====================================================

    @Test
    @DisplayName("count should return total authors")
    void count_ShouldReturnTotalAuthors() {

        long count =
                authorRepository.count();

        assertEquals(3, count);
    }
}