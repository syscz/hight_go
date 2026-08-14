package com.media.website.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlbumCreateRequestDto {
    @NotBlank(message = "相册名称不能为空")
    private String name;
    private String description;
    private String coverImage;
}
