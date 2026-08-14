package com.media.website.dto;

import lombok.Data;

@Data
public class AlbumCreateRequestDto {
    private String name;
    private String description;
    private String coverImage;
}
