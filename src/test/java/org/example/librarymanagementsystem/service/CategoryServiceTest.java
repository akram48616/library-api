package org.example.librarymanagementsystem.Service;

import org.example.librarymanagementsystem.DTO.RequestDTO.CategoryRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponseDTO categoryResponseDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Programming");
        category.setDescription("Programming books");

        categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(1);
        categoryResponseDTO.setName("Programming");
        categoryResponseDTO.setDescription("Programming books");
    }

    // ─── getByid ─────────────────────────────────
    @Test
    void getByid_whenFound_returnsDto() {
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);

        CategoryResponseDTO result = categoryService.getByid(1);

        assertEquals("Programming", result.getName());
    }

    @Test
    void getByid_whenNotFound_throwsCategoryNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getByid(999));
    }

    // ─── save ────────────────────────────────────
    @Test
    void save_whenNameUnique_createsCategory() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Programming");
        dto.setDescription("Programming books");

        when(repository.existsByName("Programming")).thenReturn(false);
        when(modelMapper.map(dto, Category.class)).thenReturn(category);
        when(repository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);

        CategoryResponseDTO result = categoryService.save(dto);

        assertEquals("Programming", result.getName());
        verify(repository).save(category);
    }

    @Test
    void save_whenNameAlreadyExists_throwsBadRequestException() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Programming");

        when(repository.existsByName("Programming")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.save(dto));
        verify(repository, never()).save(any());
    }

    // ─── update ──────────────────────────────────
    @Test
    void update_whenFound_updatesFields() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Software Engineering");
        dto.setDescription("Updated description");

        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);

        categoryService.update(1, dto);

        assertEquals("Software Engineering", category.getName());
        assertEquals("Updated description", category.getDescription());
    }

    @Test
    void update_whenNotFound_throwsCategoryNotFoundException() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(999, dto));
        verify(repository, never()).save(any());
    }

    // ─── delete ──────────────────────────────────
    @Test
    void delete_whenNoBooksLinked_deletesCategory() {
        category.setBooks(List.of());
        when(repository.findById(1)).thenReturn(Optional.of(category));

        categoryService.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_whenBooksLinked_throwsBadRequestException() {
        category.setBooks(List.of(mock(Book.class)));
        when(repository.findById(1)).thenReturn(Optional.of(category));

        assertThrows(BadRequestException.class, () -> categoryService.delete(1));
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void delete_whenNotFound_throwsCategoryNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(999));
        verify(repository, never()).deleteById(anyInt());
    }
}