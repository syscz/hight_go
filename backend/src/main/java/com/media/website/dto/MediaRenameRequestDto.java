package com.media.website.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MediaRenameRequestDto {
    @NotBlank(message = "标题不能为空")
    private String title;
}
