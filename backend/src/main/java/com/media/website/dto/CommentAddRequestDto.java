package com.media.website.dto;

import lombok.Data;

@Data
public class CommentAddRequestDto {
    private Long mediaId;
    private String content;
}
