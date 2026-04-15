package com.media.website.controller;

import com.media.website.entity.Album;
import com.media.website.service.AlbumService;
import com.media.website.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private JwtService jwtService;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return null;
        }
        return jwtService.getUserIdFromToken(token);
    }

    @GetMapping
    public ResponseEntity<List<Album>> getAlbums(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(albumService.getAlbumsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return albumService.getAlbumById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Album> createAlbum(@RequestBody Album album, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(albumService.createAlbum(album, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Album> updateAlbum(@PathVariable Long id, @RequestBody Album album, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(albumService.updateAlbum(id, album, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
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