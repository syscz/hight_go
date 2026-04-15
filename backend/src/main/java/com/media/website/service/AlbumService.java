package com.media.website.service;

import com.media.website.entity.Album;
import com.media.website.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    public List<Album> getAlbumsByUserId(Long userId) {
        return albumRepository.findByUserId(userId);
    }

    public Optional<Album> getAlbumById(Long id, Long userId) {
        Optional<Album> album = albumRepository.findById(id);
        if (album.isPresent() && !album.get().getUserId().equals(userId)) {
            return Optional.empty();
        }
        return album;
    }

    public Album createAlbum(Album album, Long userId) {
        album.setUserId(userId);
        return albumRepository.save(album);
    }

    public Album updateAlbum(Long id, Album albumDetails, Long userId) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此相册");
        }

        album.setName(albumDetails.getName());
        album.setDescription(albumDetails.getDescription());
        album.setCoverImage(albumDetails.getCoverImage());
        return albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Long id, Long userId) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此相册");
        }

        albumRepository.deleteById(id);
    }

    public Album getDefaultAlbum(Long userId) {
        return albumRepository.findByName("默认相册")
                .orElseGet(() -> {
                    Album defaultAlbum = new Album("默认相册");
                    defaultAlbum.setDescription("系统默认相册");
                    defaultAlbum.setUserId(userId);
                    return albumRepository.save(defaultAlbum);
                });
    }

    @Transactional
    public void incrementMediaCount(Long albumId) {
        albumRepository.incrementMediaCount(albumId);
    }

    @Transactional
    public void decrementMediaCount(Long albumId) {
        albumRepository.decrementMediaCount(albumId);
    }
}