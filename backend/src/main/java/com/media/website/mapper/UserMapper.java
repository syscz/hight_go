package com.media.website.mapper;

import com.media.website.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int insert(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
