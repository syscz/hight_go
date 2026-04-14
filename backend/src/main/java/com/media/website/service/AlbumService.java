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

    public List<Album> getAllAlbums() {
        getDefaultAlbum();
        return albumRepository.findAll();
    }

    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }

    public Album createAlbum(Album album) {
        return albumRepository.save(album);
    }

    public Album updateAlbum(Long id, Album albumDetails) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        album.setName(albumDetails.getName());
        album.setDescription(albumDetails.getDescription());
        album.setCoverImage(albumDetails.getCoverImage());
        return albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }

    public Album getDefaultAlbum() {
        return albumRepository.findByName("默认相册")
                .orElseGet(() -> {
                    Album defaultAlbum = new Album("默认相册");
                    defaultAlbum.setDescription("系统默认相册");
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
