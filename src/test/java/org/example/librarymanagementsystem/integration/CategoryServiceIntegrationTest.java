package org.example.librarymanagementsystem.integration;

import org.example.librarymanagementsystem.DTO.RequestDTO.CategoryRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.Service.CategoryService;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.CategoryNotFoundException;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {

        category1 = Category.builder()
                .name("Programming")
                .description("Programming Books")
                .build();

        category2 = Category.builder()
                .name("Database")
                .description("Database Books")
                .build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
    }

    // =====================================================
    // TEST 1 - getAllCategories()
    // =====================================================

    @Test
    @DisplayName("Test 1: getAllCategories should return all categories")
    void getAllCategories_ShouldReturnAllCategories() {

        List<CategoryResponseDTO> result =
                categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(c -> c.getName().equals("Programming"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(c -> c.getName().equals("Database"))
        );
    }

    // =====================================================
    // TEST 2 - getByid()
    // =====================================================

    @Test
    @DisplayName("Test 2: getByid should return category")
    void getByid_ShouldReturnCategory() {

        CategoryResponseDTO result =
                categoryService.getByid(category1.getId());

        assertNotNull(result);
        assertEquals(category1.getId(), result.getId());
        assertEquals("Programming", result.getName());
        assertEquals("Programming Books", result.getDescription());
    }

    // =====================================================
    // TEST 3 - getByid() not found
    // =====================================================

    @Test
    @DisplayName("Test 3: getByid should throw CategoryNotFoundException")
    void getByid_ShouldThrow_WhenCategoryNotFound() {

        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.getByid(99999)
                );

        assertNotNull(exception);
    }

    // =====================================================
    // TEST 4 - searchByName()
    // =====================================================

    @Test
    @DisplayName("Test 4: searchByName should return matching categories")
    void searchByName_ShouldReturnMatchingCategories() {

        List<CategoryResponseDTO> result =
                categoryService.searchByName("Prog");

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "Programming",
                result.get(0).getName()
        );
    }

    // =====================================================
    // TEST 5 - searchByName() no result
    // =====================================================

    @Test
    @DisplayName("Test 5: searchByName should return empty list")
    void searchByName_ShouldReturnEmpty_WhenNoMatch() {

        List<CategoryResponseDTO> result =
                categoryService.searchByName("Python");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =====================================================
    // TEST 6 - save()
    // =====================================================

    @Test
    @DisplayName("Test 6: save should persist new category")
    void save_ShouldPersistCategory() {

        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("Science")
                        .description("Science Books")
                        .build();

        CategoryResponseDTO result =
                categoryService.save(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Science", result.getName());
        assertEquals("Science Books", result.getDescription());

        assertTrue(
                categoryRepository.existsByName("Science")
        );
    }

    // =====================================================
    // TEST 7 - save() duplicate
    // =====================================================

    @Test
    @DisplayName("Test 7: save should throw BadRequestException for duplicate name")
    void save_ShouldThrow_WhenCategoryAlreadyExists() {

        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("Programming")
                        .description("Another description")
                        .build();

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> categoryService.save(request)
                );

        assertEquals(
                "Category 'Programming' already exists",
                exception.getMessage()
        );
    }

    // =====================================================
    // TEST 8 - update()
    // =====================================================

    @Test
    @DisplayName("Test 8: update should modify category")
    void update_ShouldModifyCategory() {

        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("Advanced Programming")
                        .description("Advanced Programming Books")
                        .build();

        CategoryResponseDTO result =
                categoryService.update(
                        category1.getId(),
                        request
                );

        assertNotNull(result);
        assertEquals(
                category1.getId(),
                result.getId()
        );
        assertEquals(
                "Advanced Programming",
                result.getName()
        );
        assertEquals(
                "Advanced Programming Books",
                result.getDescription()
        );

        Category updated =
                categoryRepository
                        .findById(category1.getId())
                        .orElseThrow();

        assertEquals(
                "Advanced Programming",
                updated.getName()
        );
    }

    // =====================================================
    // TEST 9 - update() not found
    // =====================================================

    @Test
    @DisplayName("Test 9: update should throw CategoryNotFoundException")
    void update_ShouldThrow_WhenCategoryNotFound() {

        CategoryRequestDTO request =
                CategoryRequestDTO.builder()
                        .name("New Category")
                        .description("New Description")
                        .build();

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.update(99999, request)
        );
    }

    // =====================================================
    // TEST 10 - delete()
    // =====================================================

    @Test
    @DisplayName("Test 10: delete should remove category")
    void delete_ShouldRemoveCategory() {

        Integer id = category1.getId();

        categoryService.delete(id);

        assertFalse(
                categoryRepository.existsById(id)
        );

        assertEquals(
                1,
                categoryRepository.count()
        );
    }
}