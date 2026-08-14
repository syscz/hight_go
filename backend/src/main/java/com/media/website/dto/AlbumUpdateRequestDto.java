package com.media.website.dto;

import lombok.Data;

@Data
public class AlbumUpdateRequestDto {
    private String name;
    private String description;
    private String coverImage;
}
