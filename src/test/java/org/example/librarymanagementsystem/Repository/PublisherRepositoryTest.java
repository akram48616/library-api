package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.entity.Publisher;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("datajpa")
class PublisherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PublisherRepository publisherRepository;

    private Publisher publisher1;
    private Publisher publisher2;

    @BeforeEach
    void setUp() {

        publisher1 = new Publisher();
        publisher1.setName("Oxford Press");
        publisher1.setAddress("Oxford Street, UK");
        publisher1.setPhone("9876543210");
        publisher1.setBooks(new ArrayList<>());
        entityManager.persist(publisher1);

        publisher2 = new Publisher();
        publisher2.setName("Pearson Education");
        publisher2.setAddress("London, UK");
        publisher2.setPhone("9123456789");
        publisher2.setBooks(new ArrayList<>());
        entityManager.persist(publisher2);

        entityManager.flush();
    }

    // ══════════════════════════════════════════════
    // TEST 1 — findByName returns publisher
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 1: findByName should return publisher")
    void findByName_ShouldReturnPublisher() {

        Publisher result =
                publisherRepository.findByName("Oxford Press");

        assertNotNull(result);
        assertEquals("Oxford Press", result.getName());
        assertEquals("Oxford Street, UK", result.getAddress());
    }

    // ══════════════════════════════════════════════
    // TEST 2 — findByName returns null when not found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 2: findByName should return null when not found")
    void findByName_ShouldReturnNull_WhenNotFound() {

        Publisher result =
                publisherRepository.findByName("NotExisting");

        assertNull(result);
    }

    // ══════════════════════════════════════════════
    // TEST 3 — existsByName correct boolean
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 3: existsByName should return correct boolean")
    void existsByName_ShouldReturnCorrectBoolean() {

        assertTrue(
                publisherRepository.existsByName("Oxford Press"));
        assertFalse(
                publisherRepository.existsByName("Unknown"));
    }

    // ══════════════════════════════════════════════
    // TEST 4 — findByNameContaining keyword search
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 4: findByNameContaining should find by keyword")
    void findByNameContaining_ShouldFindByKeyword() {

        // search "Oxford"
        List<Publisher> result =
                publisherRepository.findByNameContaining("Oxford");
        assertEquals(1, result.size());
        assertEquals("Oxford Press", result.get(0).getName());

        // search "Press" — finds Oxford Press
        List<Publisher> pressResult =
                publisherRepository.findByNameContaining("Press");
        assertEquals(1, pressResult.size());

        // search "xyz" — finds nothing
        List<Publisher> empty =
                publisherRepository.findByNameContaining("xyz");
        assertEquals(0, empty.size());
    }

    // ══════════════════════════════════════════════
    // TEST 5 — findByPhone returns publisher
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 5: findByPhone should return publisher")
    void findByPhone_ShouldReturnPublisher() {

        Publisher result =
                publisherRepository.findByPhone("9876543210");

        assertNotNull(result);
        assertEquals("Oxford Press", result.getName());
    }

    // ══════════════════════════════════════════════
    // TEST 6 — findByIdWithBooks returns publisher
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 6: findByIdWithBooks should return publisher")
    void findByIdWithBooks_ShouldReturnPublisher() {

        Publisher result = publisherRepository
                .findByIdWithBooks(publisher1.getId());

        assertNotNull(result);
        assertEquals("Oxford Press", result.getName());
        assertNotNull(result.getBooks());
    }

    // ══════════════════════════════════════════════
    // TEST 7 — save persists publisher
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 7: save should persist publisher")
    void save_ShouldPersistPublisher() {

        Publisher newPublisher = new Publisher();
        newPublisher.setName("McGraw Hill");
        newPublisher.setAddress("New York, USA");
        newPublisher.setPhone("9999999999");
        newPublisher.setBooks(new ArrayList<>());

        Publisher saved = publisherRepository.save(newPublisher);

        assertNotNull(saved.getId());
        assertEquals("McGraw Hill", saved.getName());
        assertEquals(3, publisherRepository.count());
    }

    // ══════════════════════════════════════════════
    // TEST 8 — deleteById removes publisher
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 8: deleteById should remove publisher")
    void deleteById_ShouldRemovePublisher() {

        Integer id = publisher1.getId();
        publisherRepository.deleteById(id);
        entityManager.flush();

        assertFalse(publisherRepository.existsById(id));
        assertEquals(1, publisherRepository.count());
    }

    // ══════════════════════════════════════════════
    // TEST 9 — update publisher fields
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 9: update should change publisher fields")
    void update_ShouldChangePublisherFields() {

        publisher1.setName("Oxford Press Updated");
        publisher1.setPhone("1111111111");
        publisherRepository.save(publisher1);
        entityManager.flush();
        entityManager.clear();

        Publisher updated = publisherRepository
                .findById(publisher1.getId()).orElse(null);

        assertNotNull(updated);
        assertEquals("Oxford Press Updated", updated.getName());
        assertEquals("1111111111", updated.getPhone());
    }

    // ══════════════════════════════════════════════
    // TEST 10 — findAll returns all publishers
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 10: findAll should return all publishers")
    void findAll_ShouldReturnAllPublishers() {

        List<Publisher> publishers =
                publisherRepository.findAll();

        assertEquals(2, publishers.size());
    }
}