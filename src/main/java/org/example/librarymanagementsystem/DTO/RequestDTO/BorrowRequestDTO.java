package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to borrow a book")
public class BorrowRequestDTO {

    @Schema(description = "ID of the book to borrow", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Book id is required")
    private Integer bookId;

    @Schema(description = "ID of the borrowing member", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Member id is required")
    private Integer memberId;

    @Schema(description = "Due date for return, must be a future date", example = "2026-09-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
}