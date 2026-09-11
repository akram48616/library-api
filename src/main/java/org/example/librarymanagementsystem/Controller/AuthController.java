package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.*;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.Service.AuthService;
import org.example.librarymanagementsystem.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Login, Refresh token, Logout endpoints"
)
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    // ─── POST /login ───────────────────────────────
    @Operation(
            summary = "Login",
            description = """
            Login with credentials.
            Returns:
            - accessToken (1 hour)
            - refreshToken (7 days)
            
            Use accessToken in Authorization header:
            Bearer <accessToken>
            
            When accessToken expires, use /refresh endpoint.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(
                            implementation =
                                    LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(
                            implementation =
                                    ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        return ResponseEntity.ok(authService.login(dto));
    }

    // ─── POST /refresh ─────────────────────────────
    @Operation(
            summary = "Refresh access token",
            description = """
            Get new access token using refresh token.
            
            When to use:
            - Access token expires (401 response)
            - Send refresh token to get new access token
            - Old refresh token is invalidated (rotation)
            - Store new refresh token for next refresh
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "New tokens generated",
                    content = @Content(schema = @Schema(
                            implementation =
                                    RefreshResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid refresh token",
                    content = @Content(schema = @Schema(
                            implementation =
                                    ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshRequestDTO dto) {

        return ResponseEntity.ok(
                authService.refresh(dto));
    }

    // ─── POST /logout ──────────────────────────────
    @Operation(
            summary = "Logout",
            description = """
            Logout user.
            
            What happens:
            - Access token added to blacklist
            - All refresh tokens revoked
            - Client must delete both tokens
            
            After logout:
            - Access token is immediately invalid
            - Refresh token cannot be used
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Logout successful"),
            @ApiResponse(responseCode = "401",
                    description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody LogoutRequestDTO dto,
            @RequestHeader("Authorization")
            String authHeader) {

        // extract token from header
        String accessToken = authHeader.substring(7).trim();

        authService.logout(accessToken, dto);

        return ResponseEntity.ok(
                "Logged out successfully. " +
                        "Please delete your tokens.");
    }

    // ─── GET /me ───────────────────────────────────
    @Operation(
            summary = "Get current user",
            description = "Returns currently logged in user info"
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body("Not authenticated");
        }

        return ResponseEntity.ok(
                java.util.Map.of(
                        "username", auth.getName(),
                        "roles", auth.getAuthorities()
                                .stream()
                                .map(a -> a.getAuthority())
                                .toList()
                )
        );
    }
}