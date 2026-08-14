package com.media.website.vo;

import com.media.website.entity.Album;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumVO {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer mediaCount;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AlbumVO from(Album album) {
        if (album == null) {
            return null;
        }
        AlbumVO vo = new AlbumVO();
        vo.setId(album.getId());
        vo.setName(album.getName());
        vo.setDescription(album.getDescription());
        vo.setCoverImage(album.getCoverImage());
        vo.setMediaCount(album.getMediaCount());
        vo.setUserId(album.getUserId());
        vo.setCreatedAt(album.getCreatedAt());
        vo.setUpdatedAt(album.getUpdatedAt());
        return vo;
    }
}
