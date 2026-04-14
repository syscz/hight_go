package com.media.website.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成文件路径
        String filePath = generateFilePath(file.getOriginalFilename());
        Path fullPath = Paths.get(uploadPath, filePath);

        // 确保文件目录存在
        Path parentDir = fullPath.getParent();
        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            log.info("创建上传子目录: {}", parentDir);
        }

        // 保存文件
        Files.write(fullPath, file.getBytes());

        // 返回文件访问URL
        return urlPrefix + "/" + filePath.replace("\\", "/");
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = FilenameUtils.getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return datePath + File.separator + uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 创建上传目录
     */
    private void createUploadDir() throws IOException {
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("创建上传目录: {}", uploadPath);
        }
    }

    /**
     * 获取文件类型
     */
    public String getFileType(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        // 图片类型
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
        for (String ext : imageExtensions) {
            if (ext.equals(extension)) {
                return "image";
            }
        }
        // 视频类型
        String[] videoExtensions = {"mp4", "avi", "mov", "wmv", "flv", "mkv"};
        for (String ext : videoExtensions) {
            if (ext.equals(extension)) {
                return "video";
            }
        }
        return "other";
    }

    /**
     * 删除文件
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadPath, filePath.replace(urlPrefix + "/", ""));
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("删除文件: {}", path);
        }
    }
}
