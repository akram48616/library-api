package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.BookRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Repository.AuthorRepository;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.CategoryRepository;
import org.example.librarymanagementsystem.Repository.PublisherRepository;
import org.example.librarymanagementsystem.entity.Author;
import org.example.librarymanagementsystem.entity.Book;
import org.example.librarymanagementsystem.entity.Category;
import org.example.librarymanagementsystem.entity.Publisher;
import org.example.librarymanagementsystem.exception.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Key;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService {
    @Autowired
    private BookRepository repository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private FileStorageService fileStorageService;

    public BookResponseDTO toDTO(Book book){
        BookResponseDTO dto=new BookResponseDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPrice(book.getPrice());
        dto.setStock(book.getStock());
        dto.setAvailable(book.getStock()>0);
        dto.setPublishedDate(LocalDate.from(book.getPublishedDate()));
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setCoverImageName(book.getCoverImageName());
        dto.setCoverImageType(book.getCoverImageType());
        dto.setCoverImageSize(book.getCoverImageSize());

        if(book.getPublisher()!=null){
            dto.setPublisher(modelMapper.map(book.getPublisher(), PublisherResponseDTO.class));
        }
        // map Authors
        if (book.getAuthors() != null)
            dto.setAuthors(book.getAuthors().stream()
                    .map(a -> modelMapper.map(a, AuthorResponseDTO.class))
                    .collect(Collectors.toList()));

        // map Categories
        if (book.getCategories() != null)
            dto.setCategories(book.getCategories().stream()
                    .map(c -> modelMapper.map(c, CategoryResponseDTO.class))
                    .collect(Collectors.toList()));
        return dto;

    }

    private PageResponseDTO<BookResponseDTO> toPageResponse(Page<Book> page){
        List<BookResponseDTO> content=page.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());
        return PageResponseDTO.<BookResponseDTO>builder()
                .content(content)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
    @Cacheable(value="books",key ="'all:' + #page + ':' + #size + ':'" +
            "+ #sortBy + ':' + #direction")

    @Transactional(readOnly = true)
    public PageResponseDTO<BookResponseDTO> getAllBooks(int page ,int size,String sortBy,String direction){
        log.info("Fetching all books - page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Sort sort=direction.equalsIgnoreCase("desc")
                ?Sort.by(sortBy).descending()
                :Sort.by(sortBy).ascending();
        Pageable pageable= PageRequest.of(page,size,sort);
        PageResponseDTO<BookResponseDTO> result = toPageResponse(repository.findAllWithDetails(pageable));
        log.debug("Fetched {} books (totalElements={})", result.getContent().size(), result.getTotalElements());
        return result;
    }
    @Cacheable(value = "book",
    key = "#id")
    @Transactional(readOnly = true)
    public BookResponseDTO getById(Integer id){
        log.info("Fetching book by id={}", id);
        Book book = repository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for id={}", id);
                    return new BookNotFoundException(id);
                });
        log.debug("Found book id={}, title='{}'", book.getId(), book.getTitle());
        return toDTO(book);
    }
    @Cacheable(
            value = "books",
            key = "'search:' + #keyword + ':' + #page + ':' + #size"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<BookResponseDTO> searchByTitle(String keyword, int page, int size) {
        log.info("Searching books by title keyword='{}', page={}, size={}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Book> result = repository.findByTitleContaining(keyword, pageable);
        log.debug("Search for '{}' returned {} results", keyword, result.getTotalElements());
        return toPageResponse(result);
    }

    // ─── GET available books ───────────────────────
    @Cacheable(
            value = "books",
            key = "'available:' + #page + ':' + #size"
    )
    @Transactional(readOnly = true)
    public PageResponseDTO<BookResponseDTO> getAvailableBooks(
            int page, int size) {
        log.info("Fetching available books - page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("title").ascending());

        Page<Book> result = repository.findByStockGreaterThan(0, pageable);
        log.debug("Found {} available books", result.getTotalElements());
        return toPageResponse(result);
    }

    // ─── GET by category ───────────────────────────
    @Cacheable(value = "books",
    key = "'category:'"+"#categoryId")
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getByCategory(Integer categoryId) {
        log.info("Fetching books by categoryId={}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Category not found for id={}", categoryId);
            throw new CategoryNotFoundException(categoryId);
        }

        List<BookResponseDTO> books = repository.findByCategoryId(categoryId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} books for categoryId={}", books.size(), categoryId);
        return books;
    }

    // ─── GET by author ─────────────────────────────
    @Cacheable(
            value = "books",
            key = "'author:' + #authorId"
    )
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getByAuthor(Integer authorId) {
        log.info("Fetching books by authorId={}", authorId);

        if (!authorRepository.existsById(authorId)) {
            log.warn("Author not found for id={}", authorId);
            throw new AuthorNotFoundException(authorId);
        }

        List<BookResponseDTO> books = repository.findByAuthorId(authorId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} books for authorId={}", books.size(), authorId);
        return books;
    }

    // ─── POST save ─────────────────────────────────
    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDTO save(BookRequestDTO dto) {
        log.info("Saving new book with isbn={}", dto.getIsbn());

        // check duplicate isbn
        if (repository.existsByIsbn(dto.getIsbn())) {
            log.warn("Duplicate ISBN on save attempt: {}", dto.getIsbn());
            throw new DuplicateIsbnException(dto.getIsbn());
        }

        // fetch publisher
        Publisher publisher = publisherRepository
                .findById(dto.getPublisherId())
                .orElseThrow(() -> {
                    log.warn("Publisher not found for id={}", dto.getPublisherId());
                    return new PublisherNotFoundException(dto.getPublisherId());
                });

        // fetch authors
        List<Author> authors = authorRepository
                .findAllById(dto.getAuthorIds());
        if (authors.isEmpty()) {
            log.warn("No authors found for provided authorIds={}", dto.getAuthorIds());
            throw new AuthorNotFoundException("No authors found for given ids");
        }

        // fetch categories (optional)
        List<Category> categoryList = null;
        if (dto.getCategoryIds() != null
                && !dto.getCategoryIds().isEmpty())
            categoryList = categoryRepository
                    .findAllById(dto.getCategoryIds());

        Set<Category> categories = categoryList != null
                ? new HashSet<>(categoryList)
                : new HashSet<>();

        // build book entity
        Book book = Book.builder()
                .title(dto.getTitle())
                .isbn(dto.getIsbn())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .publishedDate(dto.getPublishedDate())
                .publisher(publisher)
                .authors(authors)
                .categories(categories)
                .build();

        Book saved = repository.save(book);
        log.info("Book saved successfully with id={}, isbn={}", saved.getId(), saved.getIsbn());
        return toDTO(saved);
    }

    // ─── PUT update ────────────────────────────────
    @Caching(evict = {
            // evict specific book cache
            @CacheEvict(value = "book", key = "#id"),
            // evict all books list cache
            @CacheEvict(value = "books", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDTO update(Integer id, BookRequestDTO dto) {
        log.info("Updating book id={}", id);

        Book existing = repository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for update, id={}", id);
                    return new BookNotFoundException(id);
                });

        // check duplicate isbn (exclude current book)
        if (!existing.getIsbn().equals(dto.getIsbn())
                && repository.existsByIsbn(dto.getIsbn())) {
            log.warn("Duplicate ISBN on update attempt: {}", dto.getIsbn());
            throw new DuplicateIsbnException(dto.getIsbn());
        }

        // fetch publisher
        Publisher publisher = publisherRepository
                .findById(dto.getPublisherId())
                .orElseThrow(() -> {
                    log.warn("Publisher not found for id={}", dto.getPublisherId());
                    return new PublisherNotFoundException(dto.getPublisherId());
                });

        // fetch authors
        List<Author> authors = authorRepository
                .findAllById(dto.getAuthorIds());

        // fetch categories
        List<Category> categoryList = null;
        if (dto.getCategoryIds() != null
                && !dto.getCategoryIds().isEmpty())
            categoryList = categoryRepository
                    .findAllById(dto.getCategoryIds());

        Set<Category> categories = categoryList != null
                ? new HashSet<>(categoryList)
                : new HashSet<>();

        existing.setTitle(dto.getTitle());
        existing.setIsbn(dto.getIsbn());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        existing.setPublishedDate(dto.getPublishedDate());
        existing.setPublisher(publisher);
        existing.setAuthors(authors);
        existing.setCategories(categories);

        Book updated = repository.save(existing);
        log.info("Book updated successfully, id={}", updated.getId());
        return toDTO(updated);
    }

    // ─── DELETE ────────────────────────────────────
    @Caching(evict = {
            // evict specific book
            @CacheEvict(value = "book", key = "#id"),
            // evict all books list
            @CacheEvict(value = "books", allEntries = true)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        log.info("Deleting book id={}", id);

        Book book = repository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for delete, id={}", id);
                    return new BookNotFoundException(id);
                });

        if (book.getBorrowRecords() != null
                && !book.getBorrowRecords().isEmpty()) {
            log.warn("Delete blocked for book id={} — has active borrow history", id);
            throw new BadRequestException(
                    "Cannot delete book with id " + id +
                            " — has borrow history.");
        }

        repository.deleteById(id);
        log.info("Book deleted successfully, id={}", id);
    }
    @Caching(evict = {
            @CacheEvict(value = "book", key = "#Id"),
            @CacheEvict(value = "books", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDTO uploadCoverImage(Integer Id, MultipartFile file) {
        // Implementation for uploading cover image
        log.info("Uploading cover image for book id={}", Id);
        Book book = repository.findById(Id)
                .orElseThrow(() -> {
                    log.warn("Book not found for cover image upload, id={}", Id);
                    return new BookNotFoundException(Id);
                });
        if(book.getCoverImageName()!=null){
            fileStorageService.deleteFile(book.getCoverImageName());
            log.info("Cover image deleted successfully, id={}", Id);
        }
        Map<String,Object> fileInfo = fileStorageService.storeFile(file);
        book.setCoverImageUrl((String) fileInfo.get("fileUrl"));
        book.setCoverImageName((String) fileInfo.get("fileName"));
        book.setCoverImageType((String) fileInfo.get("fileType"));
        book.setCoverImageSize((Long) fileInfo.get("fileSize"));

        Book updated=repository.save(book);
        log.info("Book updated successfully, id={},"+"file={}", updated.getId(), fileInfo.get("fileName"));
        return toDTO(updated);
    }
    @Caching(evict = {
            @CacheEvict(value = "book", key = "#Id"),
            @CacheEvict(value = "books", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public BookResponseDTO deleteCoverImage(Integer Id) {
        log.info("Deleting cover image for book id={}", Id);
        Book book=repository.findByIdWithDetails(Id).orElseThrow(() -> new BookNotFoundException(Id));
        if(book.getCoverImageName()==null){

            throw new FileUploadException(
                    "Book id=" + Id +
                            " has no cover image to delete");
        }
        fileStorageService.deleteFile(
                book.getCoverImageName());

        // clear image fields in DB
        book.setCoverImageUrl(null);
        book.setCoverImageName(null);
        book.setCoverImageType(null);
        book.setCoverImageSize(null);

        Book updated = repository.save(book);

        log.info("Cover image deleted for book id={}", Id);

        return toDTO(updated);

    }
}