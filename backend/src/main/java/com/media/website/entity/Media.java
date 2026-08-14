package com.media.website.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Media {
    private Long id;

    private String filename;

    private String originalFilename;

    private String filePath;

    private String videoMp4Path;

    private Long fileSize;

    private String fileType;

    private MediaType mediaType;

    private String title;

    private String description;

    private Category category;

    private Album album;

    private Long userId;

    private String coverImage;

    private Integer viewCount;

    private Boolean isFavorite = false;

    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    private List<Tag> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum MediaType {
        image, video
    }
}
