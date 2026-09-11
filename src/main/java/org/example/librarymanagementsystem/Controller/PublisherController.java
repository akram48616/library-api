package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.PublisherRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.PublisherResponseDTO;
import org.example.librarymanagementsystem.Service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Publishers", description = "Endpoints for managing book publishers")
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    @Autowired
    private PublisherService service;

    @Operation(summary = "Get all publishers")
    @GetMapping
    public ResponseEntity<List<PublisherResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllPublishers());
    }

    @Operation(summary = "Get publisher by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher found"),
            @ApiResponse(responseCode = "404", description = "Publisher not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get publisher with books", description = "Fetches a publisher including its full list of books")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher found"),
            @ApiResponse(responseCode = "404", description = "Publisher not found", content = @Content)
    })
    @GetMapping("/{id}/books")
    public ResponseEntity<PublisherResponseDTO> getWithBooks(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByIdWithBooks(id));
    }

    @Operation(summary = "Search publishers by name")
    @GetMapping("/search")
    public ResponseEntity<List<PublisherResponseDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchByName(keyword));
    }

    @Operation(summary = "Create a new publisher")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publisher created successfully",
                    content = @Content(schema = @Schema(implementation = PublisherResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Publisher name already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PublisherResponseDTO> save(@Valid @RequestBody PublisherRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Update an existing publisher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher updated successfully"),
            @ApiResponse(responseCode = "404", description = "Publisher not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody PublisherRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete a publisher", description = "Fails if the publisher has books assigned")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publisher deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Publisher not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Publisher has books and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}