package com.media.website.controller;

import com.media.website.entity.Comment;
import com.media.website.repository.MediaRepository;
import com.media.website.service.CommentService;
import com.media.website.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final MediaRepository mediaRepository;
    private final JwtService jwtService;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return null;
        }
        return jwtService.getUserIdFromToken(token);
    }

    private String getUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtService.getUsernameFromToken(token);
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long mediaId) {
        return ResponseEntity.ok(commentService.getCommentsByMediaId(mediaId));
    }

    @PostMapping
    public ResponseEntity<?> addComment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        String username = getUsernameFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
        }

        Long mediaId = Long.parseLong(request.get("mediaId"));
        String content = request.get("content");

        Comment comment = commentService.addComment(mediaId, userId, username, content);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
        }

        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
