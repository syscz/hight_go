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

    List<Media> findByUserId(Long userId);
    List<Media> findByAlbumIdAndUserId(Long albumId, Long userId);

    List<Media> findByUserIdAndIsDeleted(Long userId, Boolean isDeleted);
    List<Media> findByUserIdAndIsFavorite(Long userId, Boolean isFavorite);
    List<Media> findByUserIdAndMediaTypeAndIsDeleted(Long userId, Media.MediaType mediaType, Boolean isDeleted);

    @Query("SELECT m FROM Media m JOIN m.tags t WHERE t.id = :tagId")
    List<Media> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT m FROM Media m WHERE m.userId = :userId AND m.isDeleted = false AND (m.title LIKE %:keyword% OR m.description LIKE %:keyword%)")
    List<Media> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT SUM(m.fileSize) FROM Media m WHERE m.userId = :userId AND m.isDeleted = false")
    Long getTotalFileSizeByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Media m WHERE m.userId = :userId AND m.isDeleted = false")
    Long countByUserIdAndNotDeleted(@Param("userId") Long userId);
}
