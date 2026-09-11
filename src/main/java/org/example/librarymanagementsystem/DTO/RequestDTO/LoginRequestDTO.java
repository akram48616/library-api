package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login request body")
public class LoginRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(
            description = "Username",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "Password",
            example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}