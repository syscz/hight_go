package com.media.website.vo;

import com.media.website.entity.Media;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MediaVO {
    private Long id;
    private String filename;
    private String originalFilename;
    private String filePath;
    private String videoMp4Path;
    private Long fileSize;
    private String fileType;
    private Media.MediaType mediaType;
    private String title;
    private String description;
    private CategoryVO category;
    private AlbumVO album;
    private Long userId;
    private String coverImage;
    private Integer duration;
    private Integer viewCount;
    private Boolean isFavorite;
    private Boolean isDeleted;
    private Boolean isPublic;
    private LocalDateTime deletedAt;
    private List<TagVO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 作者信息，仅公开广场等 join users 的查询填充 */
    private String authorName;
    private String authorAvatar;

    /** 点赞数与当前用户是否已赞（详情/广场等场景填充） */
    private Long likeCount;
    private Boolean likedByMe;

    public static MediaVO from(Media media) {
        if (media == null) {
            return null;
        }
        MediaVO vo = new MediaVO();
        vo.setId(media.getId());
        vo.setFilename(media.getFilename());
        vo.setOriginalFilename(media.getOriginalFilename());
        vo.setFilePath(media.getFilePath());
        vo.setVideoMp4Path(media.getVideoMp4Path());
        vo.setFileSize(media.getFileSize());
        vo.setFileType(media.getFileType());
        vo.setMediaType(media.getMediaType());
        vo.setTitle(media.getTitle());
        vo.setDescription(media.getDescription());
        vo.setCategory(CategoryVO.from(media.getCategory()));
        vo.setAlbum(AlbumVO.from(media.getAlbum()));
        vo.setUserId(media.getUserId());
        vo.setCoverImage(media.getCoverImage());
        vo.setDuration(media.getDuration());
        vo.setViewCount(media.getViewCount());
        vo.setIsFavorite(media.getIsFavorite());
        vo.setIsDeleted(media.getIsDeleted());
        vo.setIsPublic(media.getIsPublic());
        vo.setDeletedAt(media.getDeletedAt());
        if (media.getTags() != null) {
            vo.setTags(media.getTags().stream().map(TagVO::from).collect(Collectors.toList()));
        }
        vo.setCreatedAt(media.getCreatedAt());
        vo.setUpdatedAt(media.getUpdatedAt());
        return vo;
    }
}
