package com.media.website.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagCreateRequestDto {
    @NotBlank(message = "标签名不能为空")
    private String name;
}
