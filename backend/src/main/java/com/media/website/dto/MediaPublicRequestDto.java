package com.media.website.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MediaPublicRequestDto {
    @NotNull(message = "isPublic 不能为空")
    private Boolean isPublic;
}
