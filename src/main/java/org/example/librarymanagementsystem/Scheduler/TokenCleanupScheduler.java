package org.example.librarymanagementsystem.Scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.Service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenCleanupScheduler {

    @Autowired
    private RefreshTokenService refreshTokenService;

    // runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        log.info("Running token cleanup...");
        refreshTokenService.cleanupExpiredTokens();
    }
}