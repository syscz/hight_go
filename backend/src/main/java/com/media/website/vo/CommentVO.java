package com.media.website.vo;

import com.media.website.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private String content;
    private Long mediaId;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;

    public static CommentVO from(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setMediaId(comment.getMediaId());
        vo.setUserId(comment.getUserId());
        vo.setUsername(comment.getUsername());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}
