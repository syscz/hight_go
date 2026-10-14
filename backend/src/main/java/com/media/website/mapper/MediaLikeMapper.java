package com.media.website.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MediaLikeMapper {

    int insertIgnore(@Param("mediaId") Long mediaId, @Param("userId") Long userId);

    int deleteByMediaAndUser(@Param("mediaId") Long mediaId, @Param("userId") Long userId);

    long countByMediaId(@Param("mediaId") Long mediaId);

    boolean existsByMediaAndUser(@Param("mediaId") Long mediaId, @Param("userId") Long userId);
}
