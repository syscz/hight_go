package com.media.website.controller;

import com.media.website.dto.CommentAddRequestDto;
import com.media.website.entity.Comment;
import com.media.website.service.CommentService;
import com.media.website.util.AuthUtils;
import com.media.website.vo.ApiErrorVO;
import com.media.website.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthUtils authUtils;

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<List<CommentVO>> getComments(@PathVariable Long mediaId) {
        List<Comment> comments = commentService.getCommentsByMediaId(mediaId);
        return ResponseEntity.ok(comments.stream().map(CommentVO::from).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentAddRequestDto request,
                                        @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(new ApiErrorVO("请先登录"));
        }
        Comment comment = commentService.addComment(
                request.getMediaId(), userId, authUtils.getUsername(authHeader), request.getContent());
        return ResponseEntity.ok(CommentVO.from(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(new ApiErrorVO("请先登录"));
        }
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
