package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.BorrowRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Borrows", description = "Endpoints for borrowing, returning, and tracking book loans")
@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    @Autowired
    private BorrowService service;

    @Operation(summary = "Get all borrow records (paged)")
    @GetMapping
    public ResponseEntity<PageResponseDTO<BorrowResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "borrowDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(service.getAllBorrows(page, size, sortBy, direction));
    }

    @Operation(summary = "Get borrow record by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrow record found"),
            @ApiResponse(responseCode = "404", description = "Borrow record not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BorrowResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all overdue borrows", description = "Returns borrow records past their due date that have not been returned")
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowResponseDTO>> getOverdue() {
        return ResponseEntity.ok(service.getOverdueBorrows());
    }

    @Operation(summary = "Get borrows by member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrows found"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowResponseDTO>> getByMember(@PathVariable Integer memberId) {
        return ResponseEntity.ok(service.getByMember(memberId));
    }

    @Operation(summary = "Get borrows by book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrows found"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BorrowResponseDTO>> getByBook(@PathVariable Integer bookId) {
        return ResponseEntity.ok(service.getByBook(bookId));
    }

    @Operation(summary = "Borrow a book", description = "Creates a borrow record; requires an available book and an active member")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully",
                    content = @Content(schema = @Schema(implementation = BorrowResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book or member not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Book not available, member inactive, or already borrowed by this member", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BorrowResponseDTO> borrowBook(@Valid @RequestBody BorrowRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.borrowBook(dto));
    }

    @Operation(summary = "Return a borrowed book", description = "Marks the record as returned and applies a fine if overdue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(responseCode = "404", description = "Borrow record not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Book already returned", content = @Content)
    })
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowResponseDTO> returnBook(@PathVariable Integer id) {
        return ResponseEntity.ok(service.returnBook(id));
    }

    @Operation(summary = "[Admin] Batch-mark overdue records", description = "Scans all active borrows and flags any past due date as OVERDUE. Intended as a scheduled/admin maintenance operation, not a per-record action.")
    @ApiResponse(responseCode = "200", description = "Overdue records updated, returns count summary")
    @PutMapping("/update-overdue")
    public ResponseEntity<String> updateOverdue() {
        return ResponseEntity.ok(service.updateOverdueRecords());
    }
}