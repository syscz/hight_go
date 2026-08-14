package com.media.website.service;

import com.media.website.entity.Media;
import com.media.website.mapper.MediaMapper;
import com.media.website.mapper.TagMapper;
import com.media.website.vo.PageResultVO;
import com.media.website.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaMapper mediaMapper;
    private final AlbumService albumService;
    private final FileUploadService fileUploadService;
    private final TagMapper tagMapper;

    public List<Media> getMediaList(Long userId, Media.MediaType type, Long categoryId, Long albumId,
                                    String keyword, Boolean favorite, Boolean deleted, Long tagId) {
        if (Boolean.TRUE.equals(deleted)) {
            return mediaMapper.findByUserIdAndIsDeleted(userId, true);
        }
        if (Boolean.TRUE.equals(favorite)) {
            return mediaMapper.findByUserIdAndIsFavorite(userId, true);
        }
        if (albumId != null) {
            return mediaMapper.findByAlbumIdAndUserId(albumId, userId);
        }
        if (type != null) {
            return mediaMapper.findByUserIdAndMediaTypeAndIsDeleted(userId, type, false);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return mediaMapper.searchByKeyword(userId, keyword);
        }
        if (categoryId != null) {
            return mediaMapper.findByUserIdAndCategoryId(userId, categoryId);
        }
        if (tagId != null) {
            return mediaMapper.findByUserIdAndTagId(userId, tagId);
        }
        return mediaMapper.findByUserIdAndIsDeleted(userId, false);
    }

    public PageResultVO<Media> getMediaPage(Long userId, int page, int size, Media.MediaType type,
                                            Long categoryId, Long albumId, String keyword,
                                            Boolean favorite, Boolean deleted, Long tagId) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        int offset = (page - 1) * size;
        boolean del = Boolean.TRUE.equals(deleted);
        List<Media> list = mediaMapper.findPage(userId, del, type, categoryId, albumId,
                keyword, favorite, tagId, offset, size);
        long total = mediaMapper.countPage(userId, del, type, categoryId, albumId,
                keyword, favorite, tagId);
        return PageResultVO.of(list, total, page, size);
    }

    public Map<String, List<Media>> getTimeline(Long userId) {
        List<Media> mediaList = mediaMapper.findByUserIdAndIsDeleted(userId, false);
        return mediaList.stream().collect(Collectors.groupingBy(m -> {
            if (m.getCreatedAt() != null) {
                return m.getCreatedAt().toLocalDate().toString();
            }
            return LocalDate.now().toString();
        }));
    }

    public StatsVO getStats(Long userId) {
        List<Media> allMedia = mediaMapper.findByUserIdAndIsDeleted(userId, false);
        Long totalSize = mediaMapper.getTotalFileSizeByUserId(userId);
        Long imageCount = (long) mediaMapper.findByUserIdAndMediaTypeAndIsDeleted(userId, Media.MediaType.image, false).size();
        Long videoCount = (long) mediaMapper.findByUserIdAndMediaTypeAndIsDeleted(userId, Media.MediaType.video, false).size();
        Long favoriteCount = (long) mediaMapper.findByUserIdAndIsFavorite(userId, true).size();
        Long trashCount = (long) mediaMapper.findByUserIdAndIsDeleted(userId, true).size();

        StatsVO stats = new StatsVO();
        stats.setTotalMedia((long) allMedia.size());
        stats.setTotalSize(totalSize != null ? totalSize : 0L);
        stats.setImageCount(imageCount);
        stats.setVideoCount(videoCount);
        stats.setFavoriteCount(favoriteCount);
        stats.setTrashCount(trashCount);
        return stats;
    }

    public Optional<Media> getMediaById(Long userId, Long id) {
        return Optional.ofNullable(mediaMapper.findById(id))
                .filter(m -> userId.equals(m.getUserId()));
    }

    public void incrementViewCount(Long id) {
        mediaMapper.incrementViewCount(id);
    }

    public void setMediaTags(Long userId, Long mediaId, java.util.List<Long> tagIds) {
        getMediaById(userId, mediaId).orElseThrow(() -> new IllegalArgumentException("媒体不存在"));
        tagMapper.deleteMediaTags(mediaId);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                if (tagId != null) {
                    tagMapper.insertMediaTag(mediaId, tagId);
                }
            }
        }
    }

    public Media uploadMedia(Long userId, MultipartFile file, String title, String description,
                             Long categoryId, Long albumId) {
        try {
            String fileType = fileUploadService.getFileType(file.getOriginalFilename());
            if ("other".equals(fileType)) {
                throw new IllegalArgumentException("不支持的文件类型，仅支持图片（jpg/jpeg/png/gif/webp/bmp）和视频（mp4/avi/mov/wmv/flv/mkv）");
            }

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

            if ("image".equals(fileType)) {
                media.setMediaType(Media.MediaType.image);
                media.setCoverImage(fileUrl);
            } else if ("video".equals(fileType)) {
                media.setMediaType(Media.MediaType.video);
            }

            var album = albumId != null
                    ? albumService.getAlbumById(albumId, userId).orElse(null)
                    : null;
            if (album == null) {
                album = albumService.getDefaultAlbum(userId);
            }
            media.setAlbum(album);

            mediaMapper.insert(media);
            albumService.incrementMediaCount(album.getId());
            return media;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public Optional<Media> toggleFavorite(Long userId, Long id) {
        return getMediaById(userId, id).map(media -> {
            media.setIsFavorite(!media.getIsFavorite());
            mediaMapper.updateFavorite(id, media.getIsFavorite());
            return media;
        });
    }

    public void batchMove(Long userId, List<Long> mediaIds, Long targetAlbumId) {
        var targetAlbum = albumService.getAlbumById(targetAlbumId, userId)
                .orElseThrow(() -> new IllegalArgumentException("目标相册不存在"));
        for (Long mediaId : mediaIds) {
            getMediaById(userId, mediaId).ifPresent(media -> {
                if (media.getAlbum() != null) {
                    albumService.decrementMediaCount(media.getAlbum().getId());
                }
                media.setAlbum(targetAlbum);
                mediaMapper.updateAlbum(mediaId, targetAlbumId);
                albumService.incrementMediaCount(targetAlbumId);
            });
        }
    }

    public void batchSoftDelete(Long userId, List<Long> mediaIds) {
        for (Long mediaId : mediaIds) {
            getMediaById(userId, mediaId).ifPresent(media -> {
                media.setIsDeleted(true);
                media.setDeletedAt(LocalDate.now().atStartOfDay());
                if (media.getAlbum() != null) {
                    albumService.decrementMediaCount(media.getAlbum().getId());
                }
                mediaMapper.updateDeleted(mediaId, true, media.getDeletedAt());
            });
        }
    }

    public void batchRestore(Long userId, List<Long> mediaIds) {
        for (Long mediaId : mediaIds) {
            getMediaById(userId, mediaId).ifPresent(media -> {
                media.setIsDeleted(false);
                media.setDeletedAt(null);
                if (media.getAlbum() != null) {
                    albumService.incrementMediaCount(media.getAlbum().getId());
                }
                mediaMapper.restore(mediaId);
            });
        }
    }

    public Optional<Media> softDelete(Long userId, Long id) {
        return getMediaById(userId, id).map(media -> {
            media.setIsDeleted(true);
            media.setDeletedAt(LocalDate.now().atStartOfDay());
            if (media.getAlbum() != null) {
                albumService.decrementMediaCount(media.getAlbum().getId());
            }
            mediaMapper.updateDeleted(id, true, media.getDeletedAt());
            return media;
        });
    }

    public Optional<Media> permanentDelete(Long userId, Long id) {
        return getMediaById(userId, id).map(media -> {
            try {
                fileUploadService.deleteFile(media.getFilePath());
                mediaMapper.deleteById(id);
            } catch (IOException e) {
                throw new RuntimeException("永久删除文件失败", e);
            }
            return media;
        });
    }

    public List<Media> search(Long userId, String keyword) {
        return mediaMapper.searchByKeyword(userId, keyword);
    }
}
