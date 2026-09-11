package org.example.librarymanagementsystem.integration;

import org.example.librarymanagementsystem.DTO.RequestDTO.PublisherRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PublisherResponseDTO;
import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.Service.PublisherService;
import org.example.librarymanagementsystem.entity.Publisher;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.PublisherNotFoundException;

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
class PublisherServiceIntegrationTest {

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private PublisherRepository publisherRepository;

    private PublisherRequestDTO publisherRequestDTO;

    @BeforeEach
    void setUp() {

        publisherRequestDTO = new PublisherRequestDTO();

        publisherRequestDTO.setName("Oxford Press");
        publisherRequestDTO.setAddress("Oxford Street");
        publisherRequestDTO.setPhone("9876543210");
    }

    // ============================================================
    // TEST 1
    // ============================================================

    @Test
    @DisplayName("Test 1: save should persist publisher to real DB")
    void save_ShouldPersistPublisher_ToRealDatabase() {

        PublisherResponseDTO result =
                publisherService.save(publisherRequestDTO);

        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals("Oxford Press", result.getName());
        assertEquals("Oxford Street", result.getAddress());
        assertEquals("9876543210", result.getPhone());

        assertTrue(
                publisherRepository.existsById(result.getId())
        );

        System.out.println(
                "Test 1 passed - publisher saved successfully"
        );
    }

    // ============================================================
    // TEST 2
    // ============================================================

    @Test
    @DisplayName("Test 2: getById should return publisher")
    void getById_ShouldReturnPublisher() {

        PublisherResponseDTO saved =
                publisherService.save(publisherRequestDTO);

        PublisherResponseDTO result =
                publisherService.getById(saved.getId());

        assertNotNull(result);

        assertEquals(saved.getId(), result.getId());
        assertEquals("Oxford Press", result.getName());
        assertEquals("Oxford Street", result.getAddress());
        assertEquals("9876543210", result.getPhone());

        System.out.println(
                "Test 2 passed - publisher retrieved successfully"
        );
    }

    // ============================================================
    // TEST 3
    // ============================================================

    @Test
    @DisplayName("Test 3: getById should throw when publisher not found")
    void getById_ShouldThrow_WhenPublisherNotFound() {

        PublisherNotFoundException exception =
                assertThrows(
                        PublisherNotFoundException.class,
                        () -> publisherService.getById(99999)
                );

        assertNotNull(exception);

        System.out.println(
                "Test 3 passed - PublisherNotFoundException thrown"
        );
    }

    // ============================================================
    // TEST 4
    // ============================================================

    @Test
    @DisplayName("Test 4: getAllPublishers should return all publishers")
    void getAllPublishers_ShouldReturnAllPublishers() {

        publisherService.save(publisherRequestDTO);

        PublisherRequestDTO second =
                new PublisherRequestDTO();

        second.setName("Pearson");
        second.setAddress("London Road");
        second.setPhone("9123456780");

        publisherService.save(second);

        List<PublisherResponseDTO> result =
                publisherService.getAllPublishers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(p ->
                                p.getName().equals("Oxford Press"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(p ->
                                p.getName().equals("Pearson"))
        );

        System.out.println(
                "Test 4 passed - all publishers returned"
        );
    }

    // ============================================================
    // TEST 5
    // ============================================================

    @Test
    @DisplayName("Test 5: searchByName should return matching publishers")
    void searchByName_ShouldReturnMatchingPublishers() {

        publisherService.save(publisherRequestDTO);

        PublisherRequestDTO second =
                new PublisherRequestDTO();

        second.setName("Oxford Academic");
        second.setAddress("Academic Road");
        second.setPhone("9111111111");

        publisherService.save(second);

        List<PublisherResponseDTO> result =
                publisherService.searchByName("Oxford");

        assertNotNull(result);

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(p ->
                                p.getName().contains("Oxford"))
        );

        System.out.println(
                "Test 5 passed - publisher search works"
        );
    }

    // ============================================================
    // TEST 6
    // ============================================================

    @Test
    @DisplayName("Test 6: searchByName should return empty list")
    void searchByName_ShouldReturnEmpty_WhenNoMatch() {

        publisherService.save(publisherRequestDTO);

        List<PublisherResponseDTO> result =
                publisherService.searchByName("XYZ");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        System.out.println(
                "Test 6 passed - empty search result returned"
        );
    }

    // ============================================================
    // TEST 7
    // ============================================================

    @Test
    @DisplayName("Test 7: save should throw BadRequestException for duplicate name")
    void save_ShouldThrow_WhenPublisherNameAlreadyExists() {

        publisherService.save(publisherRequestDTO);

        PublisherRequestDTO duplicate =
                new PublisherRequestDTO();

        duplicate.setName("Oxford Press");
        duplicate.setAddress("Another Street");
        duplicate.setPhone("9000000000");

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> publisherService.save(duplicate)
                );

        assertEquals(
                "Publisher 'Oxford Press' already exists",
                exception.getMessage()
        );

        System.out.println(
                "Test 7 passed - duplicate publisher rejected"
        );
    }

    // ============================================================
    // TEST 8
    // ============================================================

    @Test
    @DisplayName("Test 8: update should change publisher details")
    void update_ShouldChangePublisherDetails() {

        PublisherResponseDTO saved =
                publisherService.save(publisherRequestDTO);

        PublisherRequestDTO updateDTO =
                new PublisherRequestDTO();

        updateDTO.setName("Oxford University Press");
        updateDTO.setAddress("New Oxford Street");
        updateDTO.setPhone("9999999999");

        PublisherResponseDTO updated =
                publisherService.update(
                        saved.getId(),
                        updateDTO
                );

        assertNotNull(updated);

        assertEquals(
                "Oxford University Press",
                updated.getName()
        );

        assertEquals(
                "New Oxford Street",
                updated.getAddress()
        );

        assertEquals(
                "9999999999",
                updated.getPhone()
        );

        System.out.println(
                "Test 8 passed - publisher updated successfully"
        );
    }

    // ============================================================
    // TEST 9
    // ============================================================

    @Test
    @DisplayName("Test 9: delete should remove publisher")
    void delete_ShouldRemovePublisher() {

        PublisherResponseDTO saved =
                publisherService.save(publisherRequestDTO);

        Integer id = saved.getId();

        publisherService.delete(id);

        assertFalse(
                publisherRepository.existsById(id)
        );

        assertThrows(
                PublisherNotFoundException.class,
                () -> publisherService.getById(id)
        );

        System.out.println(
                "Test 9 passed - publisher deleted successfully"
        );
    }

    // ============================================================
    // TEST 10
    // ============================================================

    @Test
    @DisplayName("Test 10: getByIdWithBooks should return publisher")
    void getByIdWithBooks_ShouldReturnPublisher() {

        PublisherResponseDTO saved =
                publisherService.save(publisherRequestDTO);

        PublisherResponseDTO result =
                publisherService.getByIdWithBooks(
                        saved.getId()
                );

        assertNotNull(result);

        assertEquals(
                saved.getId(),
                result.getId()
        );

        assertEquals(
                "Oxford Press",
                result.getName()
        );

        assertEquals(
                "Oxford Street",
                result.getAddress()
        );

        assertEquals(
                "9876543210",
                result.getPhone()
        );

        System.out.println(
                "Test 10 passed - publisher with books retrieved"
        );
    }
}