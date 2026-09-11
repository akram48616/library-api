package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.librarymanagementsystem.DTO.RequestDTO.MemberRequestDTO;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.MemberService;
import org.example.librarymanagementsystem.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Members", description = "Endpoints for managing library members and their status")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService service;

    @Operation(summary = "Get all members (paged)")
    @GetMapping
    public ResponseEntity<PageResponseDTO<MemberResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstname") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(service.getAllMembers(page, size, sortBy, direction));
    }

    @Operation(summary = "Get member by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get member with borrow history", description = "Fetches a member including their full borrow record list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    @GetMapping("/{id}/borrows")
    public ResponseEntity<MemberResponseDTO> getWithBorrows(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getByIdWithBorrows(id));
    }

    @Operation(summary = "Search members by name", description = "Case-insensitive partial match on first or last name, paginated")
    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<MemberResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.searchByName(keyword, page, size));
    }

    @Operation(summary = "Get members by membership status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MemberResponseDTO>> getByStatus(
            @Parameter(description = "Membership status", schema = @Schema(implementation = Member.MembershipStatus.class))
            @PathVariable Member.MembershipStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @Operation(summary = "Register a new member")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member registered successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate email or validation failure", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MemberResponseDTO> save(@Valid @RequestBody MemberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Update an existing member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member updated successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Duplicate email", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody MemberRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Suspend a member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member suspended"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    @PutMapping("/{id}/suspend")
    public ResponseEntity<MemberResponseDTO> suspend(@PathVariable Integer id) {
        return ResponseEntity.ok(service.suspendMember(id));
    }

    @Operation(summary = "Activate a member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member activated"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    @PutMapping("/{id}/activate")
    public ResponseEntity<MemberResponseDTO> activate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.activateMember(id));
    }

    @Operation(summary = "Delete a member", description = "Fails if the member has borrow history")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Member has borrow history and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}