package com.media.website.vo;

import lombok.Data;

@Data
public class StatsVO {
    private Long totalMedia;
    private Long totalSize;
    private Long imageCount;
    private Long videoCount;
    private Long favoriteCount;
    private Long trashCount;
}
