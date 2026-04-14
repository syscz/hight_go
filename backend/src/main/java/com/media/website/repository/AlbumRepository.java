package com.media.website.repository;

import com.media.website.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByName(String name);

    @Modifying
    @Query("UPDATE Album a SET a.mediaCount = a.mediaCount + 1 WHERE a.id = :id")
    void incrementMediaCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Album a SET a.mediaCount = a.mediaCount - 1 WHERE a.id = :id AND a.mediaCount > 0")
    void decrementMediaCount(@Param("id") Long id);
}
