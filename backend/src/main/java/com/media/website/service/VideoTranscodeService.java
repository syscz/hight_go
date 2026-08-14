package com.media.website.service;

import com.media.website.entity.Media;
import com.media.website.mapper.MediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频转码：ffmpeg 异步转 H.264 mp4（单线程队列，避免拖垮小服务器）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTranscodeService {

    private final FileUploadService fileUploadService;
    private final MediaMapper mediaMapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void transcodeAsync(Media media) {
        if (media == null || media.getId() == null || media.getFilePath() == null) {
            return;
        }
        if (media.getVideoMp4Path() != null) {
            return;
        }
        executor.submit(() -> {
            try {
                String mp4Path = transcode(media.getFilePath());
                if (mp4Path != null) {
                    mediaMapper.updateVideoMp4Path(media.getId(), mp4Path);
                    log.info("视频转码完成 mediaId={} -> {}", media.getId(), mp4Path);
                }
            } catch (Exception e) {
                log.error("视频转码失败 mediaId={}", media.getId(), e);
            }
        });
    }

    private String transcode(String fileUrl) throws Exception {
        Path src = fileUploadService.resolveUrl(fileUrl);
        if (!Files.exists(src)) {
            return null;
        }
        Path parent = src.getParent();
        String base = src.getFileName().toString();
        base = base.substring(0, base.lastIndexOf('.'));
        Path dst = parent.resolve(base + "_conv.mp4");
        if (Files.exists(dst)) {
            Files.delete(dst);
        }

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", src.toString(),
                "-c:v", "libx264", "-preset", "veryfast", "-crf", "28",
                "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart",
                dst.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("ffmpeg: {}", line);
            }
        }
        int code = process.waitFor();
        if (code != 0) {
            log.warn("ffmpeg 转码退出码 {}: {}", code, src);
            return null;
        }
        String datePath = parent.getFileName().toString();
        return "/uploads/" + datePath + "/" + dst.getFileName().toString();
    }
}
