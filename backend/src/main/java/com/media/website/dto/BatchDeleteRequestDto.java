package com.media.website.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteRequestDto {
    private List<Long> mediaIds;
}
