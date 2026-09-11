package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a book")
public class BookRequestDTO {

    @Schema(description = "Book title", example = "Effective Java", minLength = 1, maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be 1-200 characters")
    private String title;

    @Schema(description = "ISBN-13 identifier, must be exactly 13 digits and unique", example = "9780134685991", pattern = "^[0-9]{13}$", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9]{13}$", message = "ISBN must be exactly 13 digits")
    private String isbn;

    @Schema(description = "Book price, must be positive", example = "45.99")
    @Positive(message = "Price must be positive")
    private double price;

    @Schema(description = "Available stock count, cannot be negative", example = "5")
    @PositiveOrZero(message = "Stock cannot be negative")
    private int stock;

    @Schema(description = "Original publication date", example = "2018-01-06")
    private LocalDate publishedDate;

    @Schema(description = "ID of the book's publisher", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Publisher is required")
    private Integer publisherId;

    @Schema(description = "IDs of the book's authors, at least one required", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one author is required")
    private List<Integer> authorIds;

    @Schema(description = "IDs of the book's categories (optional)", example = "[3, 5]")
    private List<Integer> categoryIds;
}