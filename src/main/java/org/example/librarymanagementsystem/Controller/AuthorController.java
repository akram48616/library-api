package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.AuthorRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.AuthorResponseDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PageResponseDTO;
import org.example.librarymanagementsystem.Service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Authors", description = "Endpoints for managing authors and their books")
@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService service;

    @Operation(summary = "Get all authors (paged)")
    @GetMapping
    public ResponseEntity<PageResponseDTO<AuthorResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstname") String sortBy,
            @RequestParam(defaultValue = "asc") String direction){
        return ResponseEntity.ok(service.getAllAuthors(page, size, sortBy, direction));
    }

    @Operation(summary = "Get author by ID", description = "Currently returns author with books included — see note on duplicate route")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByIdWithBooks(id));
    }

    @Operation(summary = "Get author with books", description = "Fetches author including their full list of books")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @GetMapping("/{id}/books")
    public ResponseEntity<AuthorResponseDTO> getWithBooks(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByIdWithBooks(id));
    }

    @Operation(summary = "Search authors by name", description = "Case-insensitive partial match on first or last name, paginated")
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<AuthorResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.searchByName(keyword, page, size));
    }

    @Operation(summary = "Get authors by nationality")
    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<List<AuthorResponseDTO>> getByNationality(@PathVariable String nationality) {
        return ResponseEntity.ok(service.getByNationality(nationality));
    }

    @Operation(summary = "Create a new author")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate email or validation failure", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AuthorResponseDTO> save(@Valid @RequestBody AuthorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Update an existing author")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Duplicate email", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody AuthorRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete an author", description = "Fails if the author has books assigned")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Author has books and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}