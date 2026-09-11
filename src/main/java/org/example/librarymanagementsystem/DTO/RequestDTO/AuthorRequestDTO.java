package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating author details")
public class AuthorRequestDTO {

    @Schema(description = "Author's first name", example = "James", minLength = 2, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "firstname is required")
    @Size(min = 2, max = 50, message = "Firstname must be 2-50 characters")
    private String firstname;

    @Schema(description = "Author's last name", example = "Gosling", minLength = 2, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "lastname is required")
    @Size(min = 2, max = 50, message = "lastname must be 2-50 characters")
    private String lastname;

    @Schema(description = "Author's email address, must be unique", example = "james.gosling@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Schema(description = "Author's nationality", example = "Canadian", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nationality is required")
    private String nationality;
}