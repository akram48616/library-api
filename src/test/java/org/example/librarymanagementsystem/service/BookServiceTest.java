package org.example.librarymanagementsystem.service;

import org.example.librarymanagementsystem.DTO.RequestDTO.BookRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Repository.AuthorRepository;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.Service.BookService;
import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.entity.Publisher;
import org.example.librarymanagementsystem.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Publisher publisher;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1);
        publisher.setName("O'Reilly");

        author = new Author();
        author.setId(1);
        author.setFirstname("Joshua");
        author.setLastname("Bloch");

        category = new Category();
        category.setId(1);
        category.setName("Programming");

        book = Book.builder()
                .id(1)
                .title("Effective Java")
                .isbn("978-0134685991")
                .price(45.00)   // ✅ was: new BigDecimal("45.00")
                .stock(5)
                .publishedDate(LocalDate.of(2018, 1, 6))
                .publisher(publisher)
                .authors(List.of(author))
                .categories(new HashSet<>(Set.of(category)))
                .build();
    }

    // ───────────────────────── getAllBooks ─────────────────────────

    @Test
    void getAllBooks_returnsMappedPageResponse() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findAllWithDetails(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        PageResponseDTO<BookResponseDTO> result =
                bookService.getAllBooks(0, 10, "title", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Effective Java", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).isAvailable());
    }

    // ───────────────────────── getById ─────────────────────────

    @Test
    void getById_whenFound_returnsDto() {
        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        BookResponseDTO dto = bookService.getById(1);

        assertEquals(1, dto.getId());
        assertEquals("978-0134685991", dto.getIsbn());
    }

    @Test
    void getById_whenNotFound_throwsBookNotFoundException() {
        when(repository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getById(999));
    }

    // ───────────────────────── searchByTitle ─────────────────────────

    @Test
    void searchByTitle_returnsMappedPageResponse() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findByTitleContaining(eq("Effective"), any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        PageResponseDTO<BookResponseDTO> result =
                bookService.searchByTitle("Effective", 0, 10);

        assertEquals(1, result.getContent().size());
        verify(repository).findByTitleContaining(eq("Effective"), any(Pageable.class));
    }

    // ───────────────────────── getAvailableBooks ─────────────────────────

    @Test
    void getAvailableBooks_returnsOnlyInStockBooks() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findByStockGreaterThan(eq(0), any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        PageResponseDTO<BookResponseDTO> result = bookService.getAvailableBooks(0, 10);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isAvailable());
    }

    // ───────────────────────── getByCategory ─────────────────────────

    @Test
    void getByCategory_whenCategoryExists_returnsList() {
        when(categoryRepository.existsById(1)).thenReturn(true);
        when(repository.findByCategoryId(1)).thenReturn(List.of(book));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        List<BookResponseDTO> result = bookService.getByCategory(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByCategory_whenCategoryMissing_throwsCategoryNotFoundException() {
        when(categoryRepository.existsById(999)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> bookService.getByCategory(999));
        verifyNoInteractions(repository);
    }

    // ───────────────────────── getByAuthor ─────────────────────────

    @Test
    void getByAuthor_whenAuthorExists_returnsList() {
        when(authorRepository.existsById(1)).thenReturn(true);
        when(repository.findByAuthorId(1)).thenReturn(List.of(book));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        List<BookResponseDTO> result = bookService.getByAuthor(1);

        assertEquals(1, result.size());
    }

    @Test
    void getByAuthor_whenAuthorMissing_throwsAuthorNotFoundException() {
        when(authorRepository.existsById(999)).thenReturn(false);

        assertThrows(AuthorNotFoundException.class, () -> bookService.getByAuthor(999));
        verifyNoInteractions(repository);
    }

    // ───────────────────────── save ─────────────────────────

    @Test
    void save_success_createsBook() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("Effective Java");
        request.setIsbn("978-0134685991");
        request.setPrice(45.00);
        request.setStock(5);
        request.setPublishedDate(LocalDate.of(2018, 1, 6));
        request.setPublisherId(1);
        request.setAuthorIds(List.of(1));
        request.setCategoryIds(List.of(1));

        when(repository.existsByIsbn("978-0134685991")).thenReturn(false);
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(1))).thenReturn(List.of(author));
        when(categoryRepository.findAllById(List.of(1))).thenReturn(List.of(category));
        when(repository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1);
            return b;
        });
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        BookResponseDTO result = bookService.save(request);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(repository).save(any(Book.class));
    }

    @Test
    void save_whenDuplicateIsbn_throwsDuplicateIsbnException() {
        BookRequestDTO request = new BookRequestDTO();
        request.setIsbn("978-0134685991");

        when(repository.existsByIsbn("978-0134685991")).thenReturn(true);

        assertThrows(DuplicateIsbnException.class, () -> bookService.save(request));
        verify(repository, never()).save(any());
    }

    @Test
    void save_whenPublisherNotFound_throwsPublisherNotFoundException() {
        BookRequestDTO request = new BookRequestDTO();
        request.setIsbn("978-0134685991");
        request.setPublisherId(999);

        when(repository.existsByIsbn("978-0134685991")).thenReturn(false);
        when(publisherRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> bookService.save(request));
        verify(repository, never()).save(any());
    }

    @Test
    void save_whenNoAuthorsFound_throwsAuthorNotFoundException() {
        BookRequestDTO request = new BookRequestDTO();
        request.setIsbn("978-0134685991");
        request.setPublisherId(1);
        request.setAuthorIds(List.of(999));

        when(repository.existsByIsbn("978-0134685991")).thenReturn(false);
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(999))).thenReturn(Collections.emptyList());

        assertThrows(AuthorNotFoundException.class, () -> bookService.save(request));
        verify(repository, never()).save(any());
    }

    @Test
    void save_whenNoCategoryIdsProvided_savesWithEmptyCategorySet() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("Java Concurrency in Practice");
        request.setIsbn("978-0321349606");
        request.setPrice(50.00);
        request.setStock(2);
        request.setPublishedDate(LocalDate.of(2006, 5, 19));
        request.setPublisherId(1);
        request.setAuthorIds(List.of(1));
        request.setCategoryIds(null);

        when(repository.existsByIsbn("978-0321349606")).thenReturn(false);
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(1))).thenReturn(List.of(author));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());

        BookResponseDTO result = bookService.save(request);

        assertNotNull(result);
        assertTrue(result.getCategories() == null || result.getCategories().isEmpty());
        verify(categoryRepository, never()).findAllById(any());
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    void update_success_updatesExistingBook() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("Effective Java 3rd Edition");
        request.setIsbn("978-0134685991"); // same isbn, no conflict
        request.setPrice(50.00);
        request.setStock(8);
        request.setPublishedDate(LocalDate.of(2018, 1, 6));
        request.setPublisherId(1);
        request.setAuthorIds(List.of(1));
        request.setCategoryIds(List.of(1));

        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(1))).thenReturn(List.of(author));
        when(categoryRepository.findAllById(List.of(1))).thenReturn(List.of(category));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        BookResponseDTO result = bookService.update(1, request);

        assertEquals("Effective Java 3rd Edition", result.getTitle());
        assertEquals(8, result.getStock());
        verify(repository).save(book);
    }

    @Test
    void update_whenBookNotFound_throwsBookNotFoundException() {
        BookRequestDTO request = new BookRequestDTO();
        when(repository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.update(999, request));
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenIsbnChangedToDuplicate_throwsDuplicateIsbnException() {
        BookRequestDTO request = new BookRequestDTO();
        request.setIsbn("978-9999999999"); // different from existing

        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(repository.existsByIsbn("978-9999999999")).thenReturn(true);

        assertThrows(DuplicateIsbnException.class, () -> bookService.update(1, request));
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenIsbnUnchanged_doesNotCheckForDuplicate() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("Effective Java");
        request.setIsbn("978-0134685991"); // same as existing
        request.setPrice(45.00);
        request.setStock(5);
        request.setPublishedDate(LocalDate.of(2018, 1, 6));
        request.setPublisherId(1);
        request.setAuthorIds(List.of(1));
        request.setCategoryIds(List.of(1));

        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(1))).thenReturn(List.of(author));
        when(categoryRepository.findAllById(List.of(1))).thenReturn(List.of(category));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(publisher, PublisherResponseDTO.class)).thenReturn(new PublisherResponseDTO());
        when(modelMapper.map(author, AuthorResponseDTO.class)).thenReturn(new AuthorResponseDTO());
        when(modelMapper.map(category, CategoryResponseDTO.class)).thenReturn(new CategoryResponseDTO());

        bookService.update(1, request);

        verify(repository, never()).existsByIsbn(anyString());
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    void delete_whenNoBorrowHistory_deletesBook() {
        book.setBorrowRecords(Collections.emptyList());
        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));

        bookService.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_whenBookNotFound_throwsBookNotFoundException() {
        when(repository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.delete(999));
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void delete_whenBookHasBorrowHistory_throwsBadRequestException() {
        book.setBorrowRecords(List.of(mock(org.example.librarymanagementsystem.entity.BorrowRecord.class)));
        when(repository.findByIdWithDetails(1)).thenReturn(Optional.of(book));

        assertThrows(BadRequestException.class, () -> bookService.delete(1));
        verify(repository, never()).deleteById(anyInt());
    }
}