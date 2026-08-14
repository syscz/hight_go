package com.media.website.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;

    private String content;

    private Long mediaId;

    private Long userId;

    private String username;

    private LocalDateTime createdAt;
}
