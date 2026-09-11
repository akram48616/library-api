package org.example.librarymanagementsystem.DTO.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "New access token response")
public class RefreshResponseDTO implements Serializable {

    @Schema(description = "New JWT access token")
    private String accessToken;

    @Schema(description = "New refresh token")
    private String refreshToken;

    @Schema(description = "Token type",
            example = "Bearer")
    private String type;

    @Schema(description = "Access token expiry ms",
            example = "3600000")
    private long accessTokenExpiresIn;
}