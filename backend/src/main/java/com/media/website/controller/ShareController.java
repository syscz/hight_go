package com.media.website.controller;

import com.media.website.entity.Album;
import com.media.website.entity.Media;
import com.media.website.service.AlbumService;
import com.media.website.service.MediaService;
import com.media.website.vo.MediaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final AlbumService albumService;
    private final MediaService mediaService;

    /** 通过分享令牌公开访问相册（无需登录） */
    @GetMapping("/album/{token}")
    public ResponseEntity<?> getSharedAlbum(@PathVariable String token) {
        return albumService.getAlbumByToken(token)
                .<ResponseEntity<?>>map(album -> {
                    List<Media> items = mediaService.getMediaByAlbumId(album.getId());
                    Map<String, Object> body = new HashMap<>();
                    body.put("name", album.getName());
                    body.put("description", album.getDescription());
                    body.put("mediaCount", items.size());
                    body.put("items", items.stream()
                            .map(MediaVO::from).collect(Collectors.toList()));
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
