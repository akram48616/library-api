package org.example.librarymanagementsystem.DTO.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login response with JWT token")
public class LoginResponseDTO implements Serializable {

    @Schema(description = "JWT access token")
    private String token;
    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String type;

    @Schema(description = "Username", example = "admin")
    private String username;

    @Schema(description = "User role", example = "ROLE_ADMIN")
    private String role;

    @Schema(description = "Access token expiry ms",
            example = "3600000")
    private long accessTokenExpiresIn;

    @Schema(description = "Refresh token expiry ms",
            example = "604800000")
    private long refreshTokenExpiresIn;
}