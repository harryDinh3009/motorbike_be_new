package com.translateai.config.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Scheduled job để clear toàn bộ cache
 * Chạy vào lúc 1:00 AM hàng ngày
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheClearJob {

    private final CacheManager cacheManager;

    /**
     * Chạy vào lúc 1:00 AM mỗi ngày
     * Clear toàn bộ cache của translations, audio, và các cache khác
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void clearAllCaches() {
        log.info("========================================");
        log.info("Starting cache clear job at {}", LocalDateTime.now());
        log.info("========================================");

        try {
            int clearedCount = 0;

            // Clear tất cả cache names
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    clearedCount++;
                    log.info("Cleared cache: {}", cacheName);
                }
            }

            log.info("========================================");
            log.info("Cache clear completed successfully!");
            log.info("Total caches cleared: {}", clearedCount);
            log.info("Cache names: {}", cacheManager.getCacheNames());
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("Error during cache clearing: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * Clear specific cache
     */
    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Manually cleared cache: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    /**
     * Manual clear all caches (có thể gọi từ API nếu cần)
     */
    public void manualClearAll() {
        log.info("Manual cache clear triggered");
        clearAllCaches();
    }
}

