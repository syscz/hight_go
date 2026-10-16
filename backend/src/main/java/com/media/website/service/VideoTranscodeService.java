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
import java.util.stream.Collectors;

/**
 * 视频处理：ffmpeg 异步提取封面帧/时长并转 H.264 mp4（单线程队列，避免拖垮小服务器）
 * 机器上没有 ffmpeg/ffprobe 时各步骤静默跳过，不影响上传主流程
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
                Integer duration = probeDuration(media.getFilePath());
                String cover = extractCover(media.getFilePath());
                int[] coverSize = fileUploadService.readImageSize(cover);
                if (duration != null || cover != null) {
                    mediaMapper.updateCoverAndDuration(media.getId(), cover,
                            coverSize != null ? coverSize[0] : null,
                            coverSize != null ? coverSize[1] : null,
                            duration);
                    log.info("视频元数据提取完成 mediaId={} cover={} duration={}s",
                            media.getId(), cover, duration);
                }
            } catch (Exception e) {
                log.warn("视频封面/时长提取失败 mediaId={}", media.getId(), e);
            }
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
        int code = runAndWait(pb);
        if (code != 0) {
            log.warn("ffmpeg 转码退出码 {}: {}", code, src);
            return null;
        }
        String datePath = parent.getFileName().toString();
        return "/uploads/" + datePath + "/" + dst.getFileName().toString();
    }

    /**
     * 从 1 秒处抽一帧作封面（缩放到 480 宽），短视频抽不到再回落到首帧
     */
    private String extractCover(String fileUrl) {
        Path src = fileUploadService.resolveUrl(fileUrl);
        if (!Files.exists(src)) {
            return null;
        }
        String fileName = src.getFileName().toString();
        String base = fileName.substring(0, fileName.lastIndexOf('.'));
        Path dst = src.getParent().resolve(base + "_cover.jpg");
        boolean ok = runAndWait(new ProcessBuilder("ffmpeg", "-y", "-ss", "1", "-i", src.toString(),
                "-frames:v", "1", "-vf", "scale=480:-2", dst.toString())) == 0;
        if (!ok) {
            ok = runAndWait(new ProcessBuilder("ffmpeg", "-y", "-i", src.toString(),
                    "-frames:v", "1", "-vf", "scale=480:-2", dst.toString())) == 0;
        }
        if (!ok || !Files.exists(dst)) {
            return null;
        }
        String datePath = src.getParent().getFileName().toString();
        return "/uploads/" + datePath + "/" + dst.getFileName().toString();
    }

    /**
     * ffprobe 读取时长（秒），失败返回 null
     */
    private Integer probeDuration(String fileUrl) {
        try {
            Path src = fileUploadService.resolveUrl(fileUrl);
            if (!Files.exists(src)) {
                return null;
            }
            ProcessBuilder pb = new ProcessBuilder("ffprobe", "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1", src.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining()).trim();
            }
            if (process.waitFor() == 0 && !output.isEmpty() && !"N/A".equals(output)) {
                return (int) Math.round(Double.parseDouble(output));
            }
        } catch (Exception e) {
            log.debug("ffprobe 获取时长失败: {}", fileUrl);
        }
        return null;
    }

    /**
     * 运行进程并等待结束（丢弃输出防止管道阻塞），启动失败或异常返回 -1
     */
    private int runAndWait(ProcessBuilder pb) {
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // 丢弃输出，防止管道缓冲区写满阻塞
                }
            }
            return process.waitFor();
        } catch (Exception e) {
            log.warn("进程执行失败: {}", pb.command(), e);
            return -1;
        }
    }
}
