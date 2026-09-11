package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Integer> {

    // find by token string
    Optional<RefreshToken> findByToken(String token);

    // find all tokens for a user
    List<RefreshToken> findByUsername(String username);

    // find valid tokens for a user
    @Query("SELECT r FROM RefreshToken r " +
            "WHERE r.username = :username " +
            "AND r.revoked = false " +
            "AND r.expiryDate > :now")
    List<RefreshToken> findValidTokensByUsername(
            @Param("username") String username,
            @Param("now") LocalDateTime now);

    // revoke all tokens for a user (on logout)
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true " +
            "WHERE r.username = :username")
    int revokeAllUserTokens(
            @Param("username") String username);

    // delete expired tokens (cleanup)
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r " +
            "WHERE r.expiryDate < :now")
    int deleteExpiredTokens(
            @Param("now") LocalDateTime now);

    // check if token exists and is valid
    @Query("SELECT COUNT(r) > 0 FROM RefreshToken r " +
            "WHERE r.token = :token " +
            "AND r.revoked = false " +
            "AND r.expiryDate > :now")
    boolean isTokenValid(
            @Param("token") String token,
            @Param("now") LocalDateTime now);
}