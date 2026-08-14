package com.media.website.mapper;

import com.media.website.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> findByMediaIdOrderByCreatedAtDesc(@Param("mediaId") Long mediaId);

    int insert(Comment comment);

    int deleteById(@Param("id") Long id);
}
