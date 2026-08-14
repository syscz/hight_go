package com.media.website.task;

import com.media.website.entity.Media;
import com.media.website.mapper.MediaMapper;
import com.media.website.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 回收站定时清理：删除超过 30 天的回收站媒体（物理文件 + 记录）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecycleCleanupTask {

    private static final int EXPIRE_DAYS = 30;

    private final MediaMapper mediaMapper;
    private final FileUploadService fileUploadService;

    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupExpired() {
        List<Media> expired = mediaMapper.findExpiredDeleted(EXPIRE_DAYS);
        if (expired.isEmpty()) {
            return;
        }
        log.info("回收站清理：发现 {} 个过期媒体", expired.size());
        for (Media media : expired) {
            try {
                if (media.getFilePath() != null) {
                    fileUploadService.deleteFile(media.getFilePath());
                }
                if (media.getCoverImage() != null && media.getCoverImage().contains("thumb_")) {
                    fileUploadService.deleteFile(media.getCoverImage());
                }
                mediaMapper.deleteById(media.getId());
            } catch (Exception e) {
                log.error("清理媒体失败 id={}", media.getId(), e);
            }
        }
    }
}
