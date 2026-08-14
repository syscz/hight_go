package com.media.website.controller;

import com.media.website.dto.AlbumCreateRequestDto;
import com.media.website.dto.AlbumUpdateRequestDto;
import com.media.website.entity.Album;
import com.media.website.service.AlbumService;
import com.media.website.util.AuthUtils;
import com.media.website.vo.AlbumVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<AlbumVO>> getAlbums(@RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<Album> albums = albumService.getAlbumsByUserId(userId);
        return ResponseEntity.ok(albums.stream().map(AlbumVO::from).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumVO> getAlbumById(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return albumService.getAlbumById(id, userId)
                .map(a -> ResponseEntity.ok(AlbumVO.from(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AlbumVO> createAlbum(@Valid @RequestBody AlbumCreateRequestDto request,
                                               @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setCoverImage(request.getCoverImage());
        return ResponseEntity.ok(AlbumVO.from(albumService.createAlbum(album, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumVO> updateAlbum(@PathVariable Long id,
                                               @Valid @RequestBody AlbumUpdateRequestDto request,
                                               @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setCoverImage(request.getCoverImage());
        try {
            return ResponseEntity.ok(AlbumVO.from(albumService.updateAlbum(id, album, userId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            albumService.deleteAlbum(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
