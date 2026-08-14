package com.media.website.service;

import com.media.website.entity.Album;
import com.media.website.mapper.AlbumMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumMapper albumMapper;

    public List<Album> getAlbumsByUserId(Long userId) {
        return albumMapper.findByUserId(userId);
    }

    public Optional<Album> getAlbumById(Long id, Long userId) {
        Album album = albumMapper.findById(id);
        if (album != null && !album.getUserId().equals(userId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(album);
    }

    public Album createAlbum(Album album, Long userId) {
        album.setUserId(userId);
        albumMapper.insert(album);
        return album;
    }

    public Album updateAlbum(Long id, Album albumDetails, Long userId) {
        Album album = albumMapper.findById(id);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }

        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此相册");
        }

        album.setName(albumDetails.getName());
        album.setDescription(albumDetails.getDescription());
        album.setCoverImage(albumDetails.getCoverImage());
        albumMapper.update(album);
        return album;
    }

    @Transactional
    public void deleteAlbum(Long id, Long userId) {
        Album album = albumMapper.findById(id);
        if (album == null) {
            throw new RuntimeException("Album not found");
        }

        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此相册");
        }

        albumMapper.deleteById(id);
    }

    public Album getDefaultAlbum(Long userId) {
        Album album = albumMapper.findByNameAndUserId("默认相册", userId);
        if (album != null) {
            return album;
        }
        Album defaultAlbum = new Album("默认相册");
        defaultAlbum.setDescription("系统默认相册");
        defaultAlbum.setUserId(userId);
        albumMapper.insert(defaultAlbum);
        return defaultAlbum;
    }

    @Transactional
    public void incrementMediaCount(Long albumId) {
        albumMapper.incrementMediaCount(albumId);
    }

    @Transactional
    public void decrementMediaCount(Long albumId) {
        albumMapper.decrementMediaCount(albumId);
    }
}
