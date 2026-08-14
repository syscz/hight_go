package com.media.website.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchMoveRequestDto {
    @NotEmpty(message = "mediaIds 不能为空")
    private List<Long> mediaIds;

    @NotNull(message = "albumId 不能为空")
    private Long albumId;
}
