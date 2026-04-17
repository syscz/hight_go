package com.media.website.repository;

import com.media.website.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMediaIdOrderByCreatedAtDesc(Long mediaId);
}
