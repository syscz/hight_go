package com.media.website.service;

import com.media.website.entity.Comment;
import com.media.website.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;

    public List<Comment> getCommentsByMediaId(Long mediaId) {
        return commentMapper.findByMediaIdOrderByCreatedAtDesc(mediaId);
    }

    public Comment addComment(Long mediaId, Long userId, String username, String content) {
        Comment comment = new Comment();
        comment.setMediaId(mediaId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content);
        commentMapper.insert(comment);
        return comment;
    }

    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}
