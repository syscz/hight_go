package com.media.website.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteRequestDto {
    @NotEmpty(message = "mediaIds 不能为空")
    private List<Long> mediaIds;
}
