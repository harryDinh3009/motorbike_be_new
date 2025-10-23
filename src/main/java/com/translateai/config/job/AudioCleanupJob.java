package com.translateai.config.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

/**
 * Scheduled job để xóa các file audio của ngày hôm qua
 * Chạy vào lúc 1:00 AM hàng ngày
 */
@Component
@Slf4j
public class AudioCleanupJob {

    private static final String AUDIO_DIR = "audio";

    /**
     * Chạy vào lúc 1:00 AM mỗi ngày
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupYesterdayAudioFiles() {
        log.info("========================================");
        log.info("Starting audio cleanup job at {}", LocalDateTime.now());
        log.info("========================================");

        try {
            Path audioDir = Paths.get(AUDIO_DIR);

            if (!Files.exists(audioDir)) {
                log.warn("Audio directory does not exist: {}", audioDir.toAbsolutePath());
                return;
            }

            // Lấy timestamp của đầu ngày hôm nay (00:00:00)
            LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayStartMillis = startOfToday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            int deletedCount = 0;
            long deletedSize = 0;

            try (Stream<Path> files = Files.walk(audioDir)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    // Chỉ xóa file .mp3
                    if (!file.toString().toLowerCase().endsWith(".mp3")) {
                        continue;
                    }

                    long lastModified = Files.getLastModifiedTime(file).toMillis();

                    // Nếu file được tạo trước 00:00:00 hôm nay (tức là file của ngày hôm qua hoặc trước đó)
                    if (lastModified < todayStartMillis) {
                        long fileSize = Files.size(file);
                        Files.delete(file);
                        deletedCount++;
                        deletedSize += fileSize;
                        log.debug("Deleted audio file: {} (size: {} KB, last modified: {})",
                                file.getFileName(),
                                fileSize / 1024,
                                new java.util.Date(lastModified));
                    }
                }
            }

            log.info("========================================");
            log.info("Audio cleanup completed successfully!");
            log.info("Total files deleted: {}", deletedCount);
            log.info("Total size freed: {} MB", String.format("%.2f", deletedSize / 1024.0 / 1024.0));
            log.info("========================================");

        } catch (IOException e) {
            log.error("========================================");
            log.error("Error during audio files cleanup: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * Manual cleanup method (có thể gọi từ API nếu cần)
     */
    public void manualCleanup() {
        log.info("Manual cleanup triggered");
        cleanupYesterdayAudioFiles();
    }
}

