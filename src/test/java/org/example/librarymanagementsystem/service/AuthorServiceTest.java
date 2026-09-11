package org.example.librarymanagementsystem.service;

import org.example.librarymanagementsystem.DTO.RequestDTO.AuthorRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.AuthorResponseDTO;
import org.example.librarymanagementsystem.Repository.AuthorRepository;
import org.example.librarymanagementsystem.Service.AuthorService;
import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.exception.AuthorNotFoundException;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.DuplicateEmailException;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthorService authorService;

    private Author author;
    private AuthorResponseDTO authorResponseDTO;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1);
        author.setFirstname("Joshua");
        author.setLastname("Bloch");
        author.setEmail("joshua@example.com");
        author.setNationality("American");

        authorResponseDTO = new AuthorResponseDTO();
        authorResponseDTO.setId(author.getId());
        authorResponseDTO.setFirstname(author.getFirstname());
        authorResponseDTO.setLastname(author.getLastname());
        authorResponseDTO.setEmail(author.getEmail());
        authorResponseDTO.setNationality(author.getNationality());
    }

    // ─── getByIdWithBooks ────────────────────────
    @Test
    void getByIdWithBooks_whenFound_returnsDto() {
        when(repository.findByIdWithBooks(1)).thenReturn(author);
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(authorResponseDTO);

        AuthorResponseDTO result = authorService.getByIdWithBooks(1);

        assertEquals("Joshua", result.getFirstname());
    }

    @Test
    void getByIdWithBooks_whenNotFound_throwsAuthorNotFoundException() {
        when(repository.findByIdWithBooks(999)).thenReturn(null);

        assertThrows(AuthorNotFoundException.class, () -> authorService.getByIdWithBooks(999));
    }

    // ─── save ────────────────────────────────────
    @Test
    void save_whenEmailUnique_createsAuthor() {
        AuthorRequestDTO dto = new AuthorRequestDTO();
        dto.setFirstname("Joshua");
        dto.setLastname("Bloch");
        dto.setEmail("joshua@example.com");
        dto.setNationality("American");

        when(repository.existsByEmail("joshua@example.com")).thenReturn(false);
        when(modelMapper.map(dto, Author.class)).thenReturn(author);
        when(repository.save(author)).thenReturn(author);
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(authorResponseDTO);

        AuthorResponseDTO result = authorService.save(dto);

        assertEquals("Joshua", result.getFirstname());
        verify(repository).save(author);
    }

    @Test
    void save_whenEmailAlreadyExists_throwsDuplicateEmailException() {
        AuthorRequestDTO dto = new AuthorRequestDTO();
        dto.setEmail("joshua@example.com");

        when(repository.existsByEmail("joshua@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authorService.save(dto));
        verify(repository, never()).save(any());
    }

    // ─── update ──────────────────────────────────
    @Test
    void update_whenEmailUnchanged_updatesWithoutDuplicateCheck() {
        AuthorRequestDTO dto = new AuthorRequestDTO();
        dto.setFirstname("Joshua");
        dto.setLastname("Bloch Updated");
        dto.setEmail("joshua@example.com"); // same email
        dto.setNationality("American");

        when(repository.findById(1)).thenReturn(Optional.of(author));
        when(repository.save(author)).thenReturn(author);
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(authorResponseDTO);

        authorService.update(1, dto);

        assertEquals("Bloch Updated", author.getLastname());
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    void update_whenEmailChangedToDuplicate_throwsDuplicateEmailException() {
        AuthorRequestDTO dto = new AuthorRequestDTO();
        dto.setEmail("new@example.com"); // different email

        when(repository.findById(1)).thenReturn(Optional.of(author));
        when(repository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authorService.update(1, dto));
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenNotFound_throwsAuthorNotFoundException() {
        AuthorRequestDTO dto = new AuthorRequestDTO();
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.update(999, dto));
        verify(repository, never()).save(any());
    }

    // ─── delete ──────────────────────────────────
    @Test
    void delete_whenNoBooksLinked_deletesAuthor() {
        author.setBooks(List.of());
        when(repository.findById(1)).thenReturn(Optional.of(author));

        authorService.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_whenBooksLinked_throwsBadRequestException() {
        author.setBooks(List.of(mock(Book.class)));
        when(repository.findById(1)).thenReturn(Optional.of(author));

        assertThrows(BadRequestException.class, () -> authorService.delete(1));
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void delete_whenNotFound_throwsAuthorNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.delete(999));
        verify(repository, never()).deleteById(anyInt());
    }
}