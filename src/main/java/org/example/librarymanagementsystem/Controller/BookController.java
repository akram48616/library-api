package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.BookRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Books", description = "Endpoints for managing books, including search, filtering, and CRUD operations")
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService service;

    @Operation(summary = "Get all books (paged)", description = "Returns a paginated, sortable list of all books")
    @GetMapping
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> getAll(
            @Parameter(description = "Page number, 0-indexed") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(service.getAllBooks(page, size, sortBy, direction));
    }

    @Operation(summary = "Get book by ID", description = "Fetches a single book including authors, categories, and publisher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Search books by title", description = "Case-insensitive partial match on book title, paginated")
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.searchByTitle(keyword, page, size));
    }

    @Operation(summary = "Get available books", description = "Returns paginated books with stock greater than zero")
    @GetMapping("/available")
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> getAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.getAvailableBooks(page, size));
    }

    @Operation(summary = "Get books by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books found"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BookResponseDTO>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(service.getByCategory(categoryId));
    }

    @Operation(summary = "Get books by author")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books found"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BookResponseDTO>> getByAuthor(@PathVariable Integer authorId) {
        return ResponseEntity.ok(service.getByAuthor(authorId));
    }

    @Operation(summary = "Create a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate ISBN, invalid publisher/author, or validation failure", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> save(@Valid @RequestBody BookRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Update an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Duplicate ISBN or invalid data", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody BookRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete a book", description = "Fails if the book has borrow history")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Book has borrow history and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    // ─── Add to BookController.java ─────────────────

    // ─── POST upload cover image ───────────────────
    @Operation(
            summary = "Upload book cover image",
            description = """
        Upload a cover image for a book.

        Rules:
        - Max file size: 5MB
        - Allowed types: JPEG, PNG, GIF, WebP
        - Old image replaced automatically
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Image uploaded"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file / Too large"),
            @ApiResponse(responseCode = "404",
                    description = "Book not found")
    })
    @PostMapping(
            value = "/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BookResponseDTO uploadCover(
            @Parameter(description = "Book ID",
                    required = true,
                    example = "1")
            @PathVariable Integer id,

            @Parameter(description = "Cover image file " +
                    "(JPEG/PNG/GIF/WebP, max 5MB)",
                    required = true)
            @RequestParam("file") MultipartFile file) {

        return service.uploadCoverImage(id, file);
    }

    // ─── DELETE cover image ────────────────────────
    @Operation(
            summary = "Delete book cover image",
            description = "Removes the cover image from a book"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Image deleted"),
            @ApiResponse(responseCode = "400",
                    description = "No image to delete"),
            @ApiResponse(responseCode = "404",
                    description = "Book not found")
    })
    @DeleteMapping("/{id}/cover")
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponseDTO deleteCover(
            @PathVariable Integer id) {

        return service.deleteCoverImage(id);
    }
}