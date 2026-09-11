package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.DTO.RequestDTO.*;
import org.example.librarymanagementsystem.DTO.ResponseDTO.*;
import org.example.librarymanagementsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // ─── LOGIN ─────────────────────────────────────
    public LoginResponseDTO login(LoginRequestDTO dto) {

        log.info("Login attempt: {}", dto.getUsername());

        try {
            // authenticate credentials
            Authentication auth =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    dto.getUsername(),
                                    dto.getPassword()
                            )
                    );

            UserDetails userDetails =
                    (UserDetails) auth.getPrincipal();

            // generate both tokens
            String accessToken =
                    jwtUtil.generateAccessToken(userDetails);
            String refreshToken =
                    jwtUtil.generateRefreshToken(userDetails);

            // save refresh token to DB
            refreshTokenService.saveRefreshToken(
                    userDetails.getUsername(), refreshToken);

            // get role
            String role = userDetails.getAuthorities()
                    .stream().findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_USER");

            log.info("Login successful: {}, role: {}",
                    dto.getUsername(), role);

            return LoginResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .username(userDetails.getUsername())
                    .role(role)
                    .accessTokenExpiresIn(
                            jwtUtil.getAccessExpiration())
                    .refreshTokenExpiresIn(
                            jwtUtil.getRefreshExpiration())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Bad credentials: {}",
                    dto.getUsername());
            throw new BadCredentialsException(
                    "Invalid username or password");
        }
    }

    // ─── REFRESH ───────────────────────────────────
    public RefreshResponseDTO refresh(
            RefreshRequestDTO dto) {

        String refreshToken = dto.getRefreshToken();

        log.info("Refresh token request");

        // validate refresh token in DB
        if (!refreshTokenService
                .validateRefreshToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            throw new RuntimeException(
                    "Invalid or expired refresh token. " +
                            "Please login again.");
        }

        // get username from refresh token
        String username = refreshTokenService
                .getUsernameFromToken(refreshToken);

        // load user details
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        // generate new access token
        String newAccessToken =
                jwtUtil.generateAccessToken(userDetails);

        // token rotation — generate new refresh token
        String newRefreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        // revoke old refresh token
        refreshTokenService
                .revokeRefreshToken(refreshToken);

        // save new refresh token
        refreshTokenService.saveRefreshToken(
                username, newRefreshToken);

        log.info("Tokens refreshed for user: {}", username);

        return RefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .accessTokenExpiresIn(
                        jwtUtil.getAccessExpiration())
                .build();
    }

    // ─── LOGOUT ────────────────────────────────────
    public void logout(
            String accessToken,
            LogoutRequestDTO dto) {

        // get username from access token
        String username =
                jwtUtil.extractUsername(accessToken);

        log.info("Logout request for user: {}", username);

        // blacklist current access token
        java.util.Date expiry =
                jwtUtil.extractExpiration(accessToken);

        LocalDateTime expiresAt = expiry.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        refreshTokenService.blacklistAccessToken(
                accessToken, username, expiresAt);

        // revoke all refresh tokens for user
        refreshTokenService.revokeAllUserTokens(username);

        log.info("User logged out: {}", username);
    }
}