package org.example.librarymanagementsystem.Repository;

import org.example.librarymanagementsystem.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository
        extends JpaRepository<TokenBlacklist, Integer> {

    // check if token is blacklisted
    boolean existsByToken(String token);

    // delete expired blacklisted tokens (cleanup)
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t " +
            "WHERE t.tokenExpiresAt < :now")
    int deleteExpiredTokens(
            @Param("now") LocalDateTime now);
}