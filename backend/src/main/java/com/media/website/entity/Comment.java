package com.media.website.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username")
    private String username;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
