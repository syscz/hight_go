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
import java.util.List;

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
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Media> mediaList;
        if (albumId != null) {
            mediaList = mediaRepository.findByAlbumIdAndUserId(albumId, userId);
        } else if (type != null) {
            mediaList = mediaRepository.findByMediaType(type);
            mediaList = mediaList.stream().filter(m -> userId.equals(m.getUserId())).toList();
        } else if (categoryId != null) {
            mediaList = mediaRepository.findByCategoryId(categoryId);
            mediaList = mediaList.stream().filter(m -> userId.equals(m.getUserId())).toList();
        } else {
            mediaList = mediaRepository.findByUserId(userId);
        }
        return ResponseEntity.ok(mediaList);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMedia(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return mediaRepository.findById(id)
                .filter(m -> userId.equals(m.getUserId()))
                .map(media -> {
                    try {
                        fileUploadService.deleteFile(media.getFilePath());
                        if (media.getAlbum() != null) {
                            albumService.decrementMediaCount(media.getAlbum().getId());
                        }
                        mediaRepository.delete(media);
                        return ResponseEntity.noContent().build();
                    } catch (IOException e) {
                        log.error("删除文件失败", e);
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

        List<Media> mediaList = mediaRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
        mediaList = mediaList.stream().filter(m -> userId.equals(m.getUserId())).toList();
        return ResponseEntity.ok(mediaList);
    }
}