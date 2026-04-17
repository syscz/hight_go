package com.media.website.controller;

import com.media.website.entity.Album;
import com.media.website.entity.Media;
import com.media.website.repository.MediaRepository;
import com.media.website.service.AlbumService;
import com.media.website.service.FileUploadService;
import com.media.website.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private JwtService jwtService;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return null;
        }
        return jwtService.getUserIdFromToken(token);
    }

    @GetMapping
    public ResponseEntity<List<Media>> getMediaList(
            @RequestParam(required = false) Media.MediaType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean deleted,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Media> mediaList;

        if (deleted != null && deleted) {
            mediaList = mediaRepository.findByUserIdAndIsDeleted(userId, true);
        } else if (favorite != null && favorite) {
            mediaList = mediaRepository.findByUserIdAndIsFavorite(userId, true);
        } else if (albumId != null) {
            mediaList = mediaRepository.findByAlbumIdAndUserId(albumId, userId);
            mediaList = mediaList.stream().filter(m -> !m.getIsDeleted()).collect(Collectors.toList());
        } else if (type != null) {
            mediaList = mediaRepository.findByUserIdAndMediaTypeAndIsDeleted(userId, type, false);
        } else if (keyword != null && !keyword.isEmpty()) {
            mediaList = mediaRepository.searchByKeyword(userId, keyword);
        } else if (categoryId != null) {
            mediaList = mediaRepository.findByCategoryId(categoryId);
            mediaList = mediaList.stream().filter(m -> userId.equals(m.getUserId()) && !m.getIsDeleted()).collect(Collectors.toList());
        } else {
            mediaList = mediaRepository.findByUserIdAndIsDeleted(userId, false);
        }

        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/timeline")
    public ResponseEntity<Map<String, List<Media>>> getTimeline(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Media> mediaList = mediaRepository.findByUserIdAndIsDeleted(userId, false);

        Map<String, List<Media>> timeline = mediaList.stream()
                .collect(Collectors.groupingBy(m -> {
                    if (m.getCreatedAt() != null) {
                        return m.getCreatedAt().toLocalDate().toString();
                    }
                    return LocalDate.now().toString();
                }));

        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Media> allMedia = mediaRepository.findByUserIdAndIsDeleted(userId, false);
        Long totalSize = mediaRepository.getTotalFileSizeByUserId(userId);
        Long imageCount = mediaRepository.findByUserIdAndMediaTypeAndIsDeleted(userId, Media.MediaType.image, false).stream().count();
        Long videoCount = mediaRepository.findByUserIdAndMediaTypeAndIsDeleted(userId, Media.MediaType.video, false).stream().count();
        Long favoriteCount = mediaRepository.findByUserIdAndIsFavorite(userId, true).stream().count();
        Long trashCount = mediaRepository.findByUserIdAndIsDeleted(userId, true).stream().count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMedia", allMedia.size());
        stats.put("totalSize", totalSize != null ? totalSize : 0L);
        stats.put("imageCount", imageCount);
        stats.put("videoCount", videoCount);
        stats.put("favoriteCount", favoriteCount);
        stats.put("trashCount", trashCount);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMedia(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return mediaRepository.findById(id)
                .filter(m -> userId.equals(m.getUserId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Media> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String fileUrl = fileUploadService.uploadFile(file);

            Media media = new Media();
            media.setFilename(file.getOriginalFilename());
            media.setOriginalFilename(file.getOriginalFilename());
            media.setFilePath(fileUrl);
            media.setFileSize(file.getSize());
            media.setFileType(file.getContentType());
            media.setTitle(title);
            media.setDescription(description);
            media.setUserId(userId);
            media.setIsFavorite(false);
            media.setIsDeleted(false);

            String fileType = fileUploadService.getFileType(file.getOriginalFilename());
            if ("image".equals(fileType)) {
                media.setMediaType(Media.MediaType.image);
                media.setCoverImage(fileUrl);
            } else if ("video".equals(fileType)) {
                media.setMediaType(Media.MediaType.video);
            }

            Album album = null;
            if (albumId != null) {
                album = albumService.getAlbumById(albumId, userId).orElse(null);
            }
            if (album == null) {
                album = albumService.getDefaultAlbum(userId);
            }
            media.setAlbum(album);
            albumService.incrementMediaCount(album.getId());

            Media savedMedia = mediaRepository.save(media);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedia);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Media> toggleFavorite(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return mediaRepository.findById(id)
                .filter(m -> userId.equals(m.getUserId()))
                .map(media -> {
                    media.setIsFavorite(!media.getIsFavorite());
                    return ResponseEntity.ok(mediaRepository.save(media));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch/move")
    public ResponseEntity<?> batchMove(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        @SuppressWarnings("unchecked")
        List<Long> mediaIds = (List<Long>) request.get("mediaIds");
        Long targetAlbumId = ((Number) request.get("albumId")).longValue();

        Album targetAlbum = albumService.getAlbumById(targetAlbumId, userId).orElse(null);
        if (targetAlbum == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "目标相册不存在"));
        }

        for (Long mediaId : mediaIds) {
            mediaRepository.findById(mediaId)
                    .filter(m -> userId.equals(m.getUserId()))
                    .ifPresent(media -> {
                        if (media.getAlbum() != null) {
                            albumService.decrementMediaCount(media.getAlbum().getId());
                        }
                        media.setAlbum(targetAlbum);
                        albumService.incrementMediaCount(targetAlbumId);
                        mediaRepository.save(media);
                    });
        }

        return ResponseEntity.ok(Map.of("message", "移动成功"));
    }

    @PostMapping("/batch/delete")
    public ResponseEntity<?> batchSoftDelete(
            @RequestBody Map<String, List<Long>> request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Long> mediaIds = request.get("mediaIds");
        for (Long mediaId : mediaIds) {
            mediaRepository.findById(mediaId)
                    .filter(m -> userId.equals(m.getUserId()))
                    .ifPresent(media -> {
                        media.setIsDeleted(true);
                        media.setDeletedAt(LocalDate.now().atStartOfDay());
                        if (media.getAlbum() != null) {
                            albumService.decrementMediaCount(media.getAlbum().getId());
                        }
                        mediaRepository.save(media);
                    });
        }

        return ResponseEntity.ok(Map.of("message", "移入回收站成功"));
    }

    @PostMapping("/batch/restore")
    public ResponseEntity<?> batchRestore(
            @RequestBody Map<String, List<Long>> request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Long> mediaIds = request.get("mediaIds");
        for (Long mediaId : mediaIds) {
            mediaRepository.findById(mediaId)
                    .filter(m -> userId.equals(m.getUserId()))
                    .ifPresent(media -> {
                        media.setIsDeleted(false);
                        media.setDeletedAt(null);
                        if (media.getAlbum() != null) {
                            albumService.incrementMediaCount(media.getAlbum().getId());
                        }
                        mediaRepository.save(media);
                    });
        }

        return ResponseEntity.ok(Map.of("message", "恢复成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteMedia(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return mediaRepository.findById(id)
                .filter(m -> userId.equals(m.getUserId()))
                .map(media -> {
                    media.setIsDeleted(true);
                    media.setDeletedAt(LocalDate.now().atStartOfDay());
                    if (media.getAlbum() != null) {
                        albumService.decrementMediaCount(media.getAlbum().getId());
                    }
                    mediaRepository.save(media);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteMedia(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return mediaRepository.findById(id)
                .filter(m -> userId.equals(m.getUserId()))
                .map(media -> {
                    try {
                        fileUploadService.deleteFile(media.getFilePath());
                        mediaRepository.delete(media);
                        return ResponseEntity.noContent().build();
                    } catch (IOException e) {
                        log.error("永久删除文件失败", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Media>> searchMedia(@RequestParam String keyword, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Media> mediaList = mediaRepository.searchByKeyword(userId, keyword);
        return ResponseEntity.ok(mediaList);
    }
}
