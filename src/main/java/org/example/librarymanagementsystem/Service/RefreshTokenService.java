package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.Repository.RefreshTokenRepository;
import org.example.librarymanagementsystem.Repository.TokenBlacklistRepository;
import org.example.librarymanagementsystem.entity.RefreshToken;
import org.example.librarymanagementsystem.entity.TokenBlacklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenBlacklistRepository blacklistRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ─── Save refresh token to DB ──────────────────
    @Transactional
    public RefreshToken saveRefreshToken(
            String username,
            String token) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .username(username)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshExpiration / 1000))
                .build();

        RefreshToken saved =
                refreshTokenRepository.save(refreshToken);

        log.info("Refresh token saved for user: {}",
                username);

        return saved;
    }

    // ─── Validate refresh token ────────────────────
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String token) {

        return refreshTokenRepository.isTokenValid(
                token, LocalDateTime.now());
    }

    // ─── Get username from refresh token ───────────
    @Transactional(readOnly = true)
    public String getUsernameFromToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUsername)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Refresh token not found"));
    }

    // ─── Revoke refresh token (token rotation) ─────
    @Transactional
    public void revokeRefreshToken(String token) {

        refreshTokenRepository.findByToken(token)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                    log.info("Refresh token revoked for user: {}",
                            rt.getUsername());
                });
    }

    // ─── Revoke ALL tokens for user (logout) ───────
    @Transactional
    public void revokeAllUserTokens(String username) {

        int count = refreshTokenRepository
                .revokeAllUserTokens(username);

        log.info("Revoked {} refresh tokens for user: {}",
                count, username);
    }

    // ─── Blacklist access token ────────────────────
    @Transactional
    public void blacklistAccessToken(
            String token,
            String username,
            LocalDateTime expiresAt) {

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .username(username)
                .blacklistedAt(LocalDateTime.now())
                .tokenExpiresAt(expiresAt)
                .build();

        blacklistRepository.save(blacklist);

        log.info("Access token blacklisted for user: {}",
                username);
    }

    // ─── Check if access token is blacklisted ──────
    @Transactional(readOnly = true)
    public boolean isAccessTokenBlacklisted(String token) {
        return blacklistRepository.existsByToken(token);
    }

    // ─── Cleanup expired tokens ────────────────────
    @Transactional
    public void cleanupExpiredTokens() {

        int refreshDeleted = refreshTokenRepository
                .deleteExpiredTokens(LocalDateTime.now());

        int blacklistDeleted = blacklistRepository
                .deleteExpiredTokens(LocalDateTime.now());

        log.info("Cleanup: deleted {} expired refresh tokens, " +
                        "{} expired blacklisted tokens",
                refreshDeleted, blacklistDeleted);
    }
}