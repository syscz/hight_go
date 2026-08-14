package com.media.website.controller;

import com.media.website.dto.BatchDeleteRequestDto;
import com.media.website.dto.BatchMoveRequestDto;
import com.media.website.dto.BatchUpdateRequestDto;
import com.media.website.dto.TagSetRequestDto;
import com.media.website.entity.Media;
import com.media.website.service.MediaService;
import com.media.website.util.AuthUtils;
import com.media.website.vo.ApiErrorVO;
import com.media.website.vo.ApiMessageVO;
import com.media.website.vo.MediaVO;
import com.media.website.vo.PageResultVO;
import com.media.website.vo.StatsVO;
import com.media.website.service.FileUploadService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final AuthUtils authUtils;
    private final FileUploadService fileUploadService;

    @PostMapping("/download")
    public void downloadMedia(@Valid @RequestBody BatchDeleteRequestDto request,
                              @RequestHeader("Authorization") String authHeader,
                              HttpServletResponse response) throws java.io.IOException {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            response.sendError(401);
            return;
        }
        var mediaList = mediaService.getMediaByIds(userId, request.getMediaIds());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=media.zip");
        try (var zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
            for (var media : mediaList) {
                var path = fileUploadService.resolveUrl(media.getFilePath());
                if (java.nio.file.Files.exists(path)) {
                    String name = media.getOriginalFilename() != null ? media.getOriginalFilename() : media.getFilename();
                    zos.putNextEntry(new java.util.zip.ZipEntry(name));
                    java.nio.file.Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    @PostMapping("/batch/update")
    public ResponseEntity<?> batchUpdate(@Valid @RequestBody BatchUpdateRequestDto request,
                                         @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            mediaService.batchUpdate(userId, request.getMediaIds(), request.getTitle(),
                    request.getDescription(), request.getTagIds());
            return ResponseEntity.ok(new ApiMessageVO("更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMediaList(
            @RequestParam(required = false) Media.MediaType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (page != null) {
            int pageSize = size != null ? size : 24;
            PageResultVO<Media> result = mediaService.getMediaPage(
                    userId, page, pageSize, type, categoryId, albumId, keyword, favorite, deleted, tagId);
            PageResultVO<MediaVO> vo = PageResultVO.of(
                    result.getList().stream().map(MediaVO::from).collect(Collectors.toList()),
                    result.getTotal(), result.getPage(), result.getSize());
            return ResponseEntity.ok(vo);
        }

        List<Media> mediaList = mediaService.getMediaList(
                userId, type, categoryId, albumId, keyword, favorite, deleted, tagId);
        return ResponseEntity.ok(mediaList.stream().map(MediaVO::from).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<?> setMediaTags(@PathVariable Long id,
                                          @Valid @RequestBody TagSetRequestDto request,
                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            mediaService.setMediaTags(userId, id, request.getTagIds());
            return ResponseEntity.ok(new ApiMessageVO("标签已更新"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @GetMapping("/timeline")
    public ResponseEntity<Map<String, List<MediaVO>>> getTimeline(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, List<Media>> timeline = mediaService.getTimeline(userId);
        Map<String, List<MediaVO>> vo = timeline.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().map(MediaVO::from).collect(Collectors.toList())));
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsVO> getStats(@RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mediaService.getStats(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaVO> getMedia(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        var media = mediaService.getMediaById(userId, id);
        media.ifPresent(m -> {
            mediaService.incrementViewCount(id);
            m.setViewCount((m.getViewCount() == null ? 0 : m.getViewCount()) + 1);
        });
        return media
                .map(m -> ResponseEntity.ok(MediaVO.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long albumId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Media saved = mediaService.uploadMedia(userId, file, title, description, categoryId, albumId);
            return ResponseEntity.status(HttpStatus.CREATED).body(MediaVO.from(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<MediaVO> toggleFavorite(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return mediaService.toggleFavorite(userId, id)
                .map(m -> ResponseEntity.ok(MediaVO.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch/move")
    public ResponseEntity<?> batchMove(@Valid @RequestBody BatchMoveRequestDto request,
                                       @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getMediaIds() == null || request.getAlbumId() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorVO("参数不完整"));
        }
        try {
            mediaService.batchMove(userId, request.getMediaIds(), request.getAlbumId());
            return ResponseEntity.ok(new ApiMessageVO("移动成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @PostMapping("/batch/delete")
    public ResponseEntity<?> batchSoftDelete(@Valid @RequestBody BatchDeleteRequestDto request,
                                             @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getMediaIds() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorVO("参数不完整"));
        }
        mediaService.batchSoftDelete(userId, request.getMediaIds());
        return ResponseEntity.ok(new ApiMessageVO("移入回收站成功"));
    }

    @PostMapping("/batch/restore")
    public ResponseEntity<?> batchRestore(@Valid @RequestBody BatchDeleteRequestDto request,
                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getMediaIds() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorVO("参数不完整"));
        }
        mediaService.batchRestore(userId, request.getMediaIds());
        return ResponseEntity.ok(new ApiMessageVO("恢复成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMedia(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        var media = mediaService.softDelete(userId, id);
        if (media.isPresent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDeleteMedia(@PathVariable Long id,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            var media = mediaService.permanentDelete(userId, id);
            if (media.isPresent()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<MediaVO>> searchMedia(@RequestParam String keyword,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<Media> mediaList = mediaService.search(userId, keyword);
        return ResponseEntity.ok(mediaList.stream().map(MediaVO::from).collect(Collectors.toList()));
    }
}
