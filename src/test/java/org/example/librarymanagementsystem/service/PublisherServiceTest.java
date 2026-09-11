package org.example.librarymanagementsystem.service;

import org.example.librarymanagementsystem.DTO.RequestDTO.PublisherRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PublisherResponseDTO;
import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.Service.PublisherService;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.Publisher;
import org.example.librarymanagementsystem.exception.BadRequestException;
import org.example.librarymanagementsystem.exception.PublisherNotFoundException;
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
class PublisherServiceTest {

    @Mock
    private PublisherRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PublisherService publisherService;

    private Publisher publisher;
    private PublisherResponseDTO publisherResponseDTO;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1);
        publisher.setName("O'Reilly");
        publisher.setAddress("Sebastopol, CA");
        publisher.setPhone("1234567890");

        publisherResponseDTO = new PublisherResponseDTO();
        publisherResponseDTO.setId(1);
        publisherResponseDTO.setName("O'Reilly");
    }

    // ─── getById ─────────────────────────────────
    @Test
    void getById_whenFound_returnsDto() {
        when(repository.findById(1)).thenReturn(Optional.of(publisher));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(publisherResponseDTO);

        PublisherResponseDTO result = publisherService.getById(1);

        assertEquals("O'Reilly", result.getName());
    }

    @Test
    void getById_whenNotFound_throwsPublisherNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.getById(999));
    }

    // ─── getByIdWithBooks ────────────────────────
    @Test
    void getByIdWithBooks_whenNotFound_throwsPublisherNotFoundException() {
        when(repository.findByIdWithBooks(999)).thenReturn(null);

        assertThrows(PublisherNotFoundException.class, () -> publisherService.getByIdWithBooks(999));
    }

    // ─── save ────────────────────────────────────
    @Test
    void save_whenNameUnique_createsPublisher() {
        PublisherRequestDTO dto = new PublisherRequestDTO();
        dto.setName("O'Reilly");
        dto.setAddress("Sebastopol, CA");
        dto.setPhone("1234567890");

        when(repository.existsByName("O'Reilly")).thenReturn(false);
        when(modelMapper.map(dto, Publisher.class)).thenReturn(publisher);
        when(repository.save(publisher)).thenReturn(publisher);
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(publisherResponseDTO);

        PublisherResponseDTO result = publisherService.save(dto);

        assertEquals("O'Reilly", result.getName());
        verify(repository).save(publisher);
    }

    @Test
    void save_whenNameAlreadyExists_throwsBadRequestException() {
        PublisherRequestDTO dto = new PublisherRequestDTO();
        dto.setName("O'Reilly");

        when(repository.existsByName("O'Reilly")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> publisherService.save(dto));
        verify(repository, never()).save(any());
    }

    // ─── update ──────────────────────────────────
    @Test
    void update_whenFound_updatesFields() {
        PublisherRequestDTO dto = new PublisherRequestDTO();
        dto.setName("O'Reilly Media");
        dto.setAddress("New Address");
        dto.setPhone("9999999999");

        when(repository.findById(1)).thenReturn(Optional.of(publisher));
        when(repository.save(publisher)).thenReturn(publisher);
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(publisherResponseDTO);

        publisherService.update(1, dto);

        assertEquals("O'Reilly Media", publisher.getName());
        assertEquals("New Address", publisher.getAddress());
    }

    @Test
    void update_whenNotFound_throwsPublisherNotFoundException() {
        PublisherRequestDTO dto = new PublisherRequestDTO();
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.update(999, dto));
        verify(repository, never()).save(any());
    }

    // ─── delete ──────────────────────────────────
    @Test
    void delete_whenNoBooksLinked_deletesPublisher() {
        publisher.setBooks(List.of());
        when(repository.findById(1)).thenReturn(Optional.of(publisher));

        publisherService.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_whenBooksLinked_throwsBadRequestException() {
        publisher.setBooks(List.of(mock(Book.class)));
        when(repository.findById(1)).thenReturn(Optional.of(publisher));

        assertThrows(BadRequestException.class, () -> publisherService.delete(1));
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void delete_whenNotFound_throwsPublisherNotFoundException() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.delete(999));
        verify(repository, never()).deleteById(anyInt());
    }
}