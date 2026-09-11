package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Logout request")
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh token is required")
    @Schema(
            description = "Refresh token to invalidate",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}