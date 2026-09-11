package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.CategoryRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.librarymanagementsystem.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Endpoints for managing book categories")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService service;

    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @Operation(summary = "Get category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByid(id));
    }

    @Operation(summary = "Get category by ID (duplicate route)", description = "Currently identical to GET /{id} — service layer has no books-eager-fetch query behind this route despite the path name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @GetMapping("/{id}/books")
    public ResponseEntity<CategoryResponseDTO> getWithBooks(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByid(id));
    }

    @Operation(summary = "Search categories by name")
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchByName(keyword));
    }

    @Operation(summary = "Create a new category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Category name already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> save(@Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Update an existing category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Delete a category", description = "Fails if the category has books assigned")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Category has books and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}