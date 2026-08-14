package com.media.website.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Category {
    private Long id;

    private String name;

    private String description;

    @JsonIgnore
    private List<Media> mediaList;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
