package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.entity.Category;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("datajpa")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;
    private Category category3;

    @BeforeEach
    void setUp() {

        category1 = new Category();
        category1.setName("Programming");
        category1.setDescription("Programming books");
        category1.setBooks(new ArrayList<>());
        entityManager.persist(category1);

        category2 = new Category();
        category2.setName("Fiction");
        category2.setDescription("Fictional novels");
        category2.setBooks(new ArrayList<>());
        entityManager.persist(category2);

        category3 = new Category();
        category3.setName("Science");
        category3.setDescription("Science books");
        category3.setBooks(new ArrayList<>());
        entityManager.persist(category3);

        entityManager.flush();
    }

    // ══════════════════════════════════════════════
    // TEST 1 — findByName returns correct category
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 1: findByName should return category")
    void findByName_ShouldReturnCategory() {

        Category result =
                categoryRepository.findByName("Programming");

        assertNotNull(result);
        assertEquals("Programming", result.getName());
        assertEquals("Programming books",
                result.getDescription());
    }

    // ══════════════════════════════════════════════
    // TEST 2 — findByName returns null when not found
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 2: findByName should return null when not found")
    void findByName_ShouldReturnNull_WhenNotFound() {

        Category result =
                categoryRepository.findByName("NotExisting");

        assertNull(result);
    }

    // ══════════════════════════════════════════════
    // TEST 3 — existsByName correct boolean
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 3: existsByName should return correct boolean")
    void existsByName_ShouldReturnCorrectBoolean() {

        assertTrue(categoryRepository.existsByName("Programming"));
        assertTrue(categoryRepository.existsByName("Fiction"));
        assertFalse(categoryRepository.existsByName("History"));
    }

    // ══════════════════════════════════════════════
    // TEST 4 — findByNameContaining keyword search
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 4: findByNameContaining should find by keyword")
    void findByNameContaining_ShouldFindByKeyword() {

        // search "Prog" → finds Programming
        List<Category> result =
                categoryRepository.findByNameContaining("Prog");

        assertEquals(1, result.size());
        assertEquals("Programming", result.get(0).getName());

        // search "i" → finds Programming, Fiction, Science
        List<Category> iResult =
                categoryRepository.findByNameContaining("i");

        assertEquals(3, iResult.size());

        // search "xyz" → finds nothing
        List<Category> empty =
                categoryRepository.findByNameContaining("xyz");

        assertTrue(empty.isEmpty());
    }

    // ══════════════════════════════════════════════
    // TEST 5 — findByIdWithBooks returns category
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 5: findByIdWithBooks should return category with books")
    void findByIdWithBooks_ShouldReturnCategory() {

        Category result = categoryRepository
                .findByIdWithBooks(category1.getId());

        assertNotNull(result);
        assertEquals("Programming", result.getName());
        assertNotNull(result.getBooks());
    }

    // ══════════════════════════════════════════════
    // TEST 6 — findAll returns all categories
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 6: findAll should return all categories")
    void findAll_ShouldReturnAllCategories() {

        List<Category> categories =
                categoryRepository.findAll();

        assertEquals(3, categories.size());
    }

    // ══════════════════════════════════════════════
    // TEST 7 — save persists category
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 7: save should persist category")
    void save_ShouldPersistCategory() {

        Category newCategory = new Category();
        newCategory.setName("History");
        newCategory.setDescription("History books");
        newCategory.setBooks(new ArrayList<>());

        Category saved = categoryRepository.save(newCategory);

        assertNotNull(saved.getId());
        assertEquals("History", saved.getName());
        assertEquals(4, categoryRepository.count());
    }

    // ══════════════════════════════════════════════
    // TEST 8 — deleteById removes category
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 8: deleteById should remove category")
    void deleteById_ShouldRemoveCategory() {

        Integer id = category1.getId();
        categoryRepository.deleteById(id);
        entityManager.flush();

        assertFalse(categoryRepository.existsById(id));
        assertEquals(2, categoryRepository.count());
    }

    // ══════════════════════════════════════════════
    // TEST 9 — update category name
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 9: update should change category name")
    void update_ShouldChangeCategoryName() {

        category1.setName("Programming Updated");
        category1.setDescription("Updated description");
        categoryRepository.save(category1);
        entityManager.flush();
        entityManager.clear();

        Category updated = categoryRepository
                .findById(category1.getId()).orElse(null);

        assertNotNull(updated);
        assertEquals("Programming Updated", updated.getName());
        assertEquals("Updated description",
                updated.getDescription());
    }

    // ══════════════════════════════════════════════
    // TEST 10 — count returns correct number
    // ══════════════════════════════════════════════
    @Test
    @DisplayName("Test 10: count should return correct number")
    void count_ShouldReturnCorrectNumber() {

        assertEquals(3, categoryRepository.count());

        // add one more
        Category extra = new Category();
        extra.setName("Extra");
        extra.setBooks(new ArrayList<>());
        categoryRepository.save(extra);

        assertEquals(4, categoryRepository.count());
    }
}