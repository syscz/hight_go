package com.media.website.repository;

import com.media.website.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByMediaType(Media.MediaType mediaType);
    List<Media> findByCategoryId(Long categoryId);
    List<Media> findByAlbumId(Long albumId);
    List<Media> findByTitleContainingOrDescriptionContaining(String title, String description);
    
    @Query("SELECT m FROM Media m JOIN m.tags t WHERE t.id = :tagId")
    List<Media> findByTagId(@Param("tagId") Long tagId);
}
