package com.media.website.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchMoveRequestDto {
    private List<Long> mediaIds;
    private Long albumId;
}
