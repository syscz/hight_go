package com.media.website.mapper;

import com.media.website.entity.Album;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbumMapper {

    List<Album> findByUserId(@Param("userId") Long userId);

    Album findById(@Param("id") Long id);

    Album findByName(@Param("name") String name);

    Album findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    int insert(Album album);

    int update(Album album);

    int deleteById(@Param("id") Long id);

    int incrementMediaCount(@Param("id") Long id);

    int decrementMediaCount(@Param("id") Long id);
}
