package com.media.website.mapper;

import com.media.website.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    List<Tag> findAll();

    Tag findById(@Param("id") Long id);

    List<Tag> findByMediaId(@Param("mediaId") Long mediaId);
}
