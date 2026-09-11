package org.example.librarymanagementsystem.DTO.RequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Refresh token Request")
public class RefreshRequestDTO {
    @NotBlank(message = "Refresh token is request")
    @Schema(description = "Refresh token from login response")
    private String refreshToken;
}
