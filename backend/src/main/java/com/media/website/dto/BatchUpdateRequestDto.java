package com.media.website.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateRequestDto {
    @NotEmpty(message = "mediaIds 不能为空")
    private List<Long> mediaIds;

    private String title;
    private String description;
    private List<Long> tagIds;
}
