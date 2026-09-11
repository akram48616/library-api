package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a category")
public class CategoryRequestDTO {

    @Schema(description = "Category name, must be unique", example = "Science Fiction", minLength = 1, maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be 1-200 characters")
    private String name;

    @Schema(description = "Category description", example = "Speculative fiction dealing with futuristic concepts, technology, and society", maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}