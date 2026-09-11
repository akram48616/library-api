package org.example.librarymanagementsystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ─── Signing key ───────────────────────────────
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                Base64.getEncoder()
                        .encodeToString(secret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ─── Generate ACCESS token ─────────────────────
    public String generateAccessToken(
            UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .toList());

        return buildToken(claims,
                userDetails.getUsername(),
                accessExpiration);
    }

    // ─── Generate REFRESH token ────────────────────
    public String generateRefreshToken(
            UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return buildToken(claims,
                userDetails.getUsername(),
                refreshExpiration);
    }

    // ─── Build token ───────────────────────────────
    private String buildToken(
            Map<String, Object> claims,
            String username,
            long expiry) {

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    // ─── Extract username ──────────────────────────
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ─── Extract expiration ────────────────────────
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─── Extract any claim ─────────────────────────
    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // ─── Extract all claims ────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ─── Check if expired ──────────────────────────
    public boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    // ─── Validate ACCESS token ─────────────────────
    public boolean validateToken(
            String token,
            UserDetails userDetails) {

        try {
            String username = extractUsername(token);
            return username.equals(
                    userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (ExpiredJwtException e) {
            log.warn("Access token expired: {}",
                    e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid token: {}",
                    e.getMessage());
        } catch (Exception e) {
            log.error("Token validation error: {}",
                    e.getMessage());
        }
        return false;
    }

    // ─── Validate REFRESH token ────────────────────
    public boolean validateRefreshToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh token error: {}",
                    e.getMessage());
            return false;
        }
    }

    // ─── Getters ───────────────────────────────────
    public long getAccessExpiration() {
        return accessExpiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}