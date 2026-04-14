package com.media.website.controller;

import com.media.website.entity.Album;
import com.media.website.entity.Media;
import com.media.website.repository.MediaRepository;
import com.media.website.service.AlbumService;
import com.media.website.service.FileUploadService;
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
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private AlbumService albumService;

    @GetMapping
    public ResponseEntity<List<Media>> getMediaList(
            @RequestParam(required = false) Media.MediaType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId) {
        List<Media> mediaList;
        if (albumId != null) {
            mediaList = mediaRepository.findByAlbumId(albumId);
        } else if (type != null) {
            mediaList = mediaRepository.findByMediaType(type);
        } else if (categoryId != null) {
            mediaList = mediaRepository.findByCategoryId(categoryId);
        } else {
            mediaList = mediaRepository.findAll();
        }
        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMedia(@PathVariable Long id) {
        return mediaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Media> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId) {
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

            String fileType = fileUploadService.getFileType(file.getOriginalFilename());
            if ("image".equals(fileType)) {
                media.setMediaType(Media.MediaType.image);
                media.setCoverImage(fileUrl);
            } else if ("video".equals(fileType)) {
                media.setMediaType(Media.MediaType.video);
            }

            Album album = null;
            if (albumId != null) {
                album = albumService.getAlbumById(albumId).orElse(null);
            }
            if (album == null) {
                album = albumService.getDefaultAlbum();
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
    public ResponseEntity<Object> deleteMedia(@PathVariable Long id) {
        return mediaRepository.findById(id)
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
    public ResponseEntity<List<Media>> searchMedia(@RequestParam String keyword) {
        List<Media> mediaList = mediaRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
        return ResponseEntity.ok(mediaList);
    }
}
