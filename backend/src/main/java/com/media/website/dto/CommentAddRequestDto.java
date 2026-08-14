package com.media.website.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentAddRequestDto {
    @NotNull(message = "mediaId 不能为空")
    private Long mediaId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
