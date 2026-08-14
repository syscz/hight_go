package com.media.website.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Tag {
    private Long id;

    private String name;

    @JsonIgnore
    private List<Media> mediaList;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
