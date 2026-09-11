package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a publisher")
public class PublisherRequestDTO {

    @Schema(description = "Publisher name, must be unique", example = "O'Reilly Media", minLength = 2, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Publisher name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @Schema(description = "Publisher's mailing address", example = "1005 Gravenstein Hwy N, Sebastopol, CA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Address is required")
    private String address;

    @Schema(description = "Publisher's contact phone, exactly 10 digits", example = "8001234567", pattern = "^[0-9]{10}$", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "PhoneNumber is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;
}