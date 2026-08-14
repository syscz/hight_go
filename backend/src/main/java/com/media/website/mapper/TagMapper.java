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

    Tag findByName(@Param("name") String name);

    int insert(Tag tag);

    int insertMediaTag(@Param("mediaId") Long mediaId, @Param("tagId") Long tagId);

    int deleteMediaTags(@Param("mediaId") Long mediaId);
}
