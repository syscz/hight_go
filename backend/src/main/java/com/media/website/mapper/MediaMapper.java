package com.media.website.mapper;

import com.media.website.entity.Media;
import com.media.website.vo.MediaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MediaMapper {

    Media findById(@Param("id") Long id);

    List<Media> findByUserIdAndIsDeleted(@Param("userId") Long userId, @Param("isDeleted") boolean isDeleted);

    List<Media> findByUserIdAndIsFavorite(@Param("userId") Long userId, @Param("isFavorite") boolean isFavorite);

    List<Media> findByAlbumIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);

    List<Media> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    List<Media> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    List<Media> findByUserIdAndMediaTypeAndIsDeleted(@Param("userId") Long userId,
                                                     @Param("mediaType") Media.MediaType mediaType,
                                                     @Param("isDeleted") boolean isDeleted);

    List<Media> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    List<Media> findPage(@Param("userId") Long userId, @Param("deleted") boolean deleted,
                         @Param("type") Media.MediaType type, @Param("categoryId") Long categoryId,
                         @Param("albumId") Long albumId, @Param("keyword") String keyword,
                         @Param("favorite") Boolean favorite, @Param("tagId") Long tagId,
                         @Param("sort") String sort,
                         @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("userId") Long userId, @Param("deleted") boolean deleted,
                   @Param("type") Media.MediaType type, @Param("categoryId") Long categoryId,
                   @Param("albumId") Long albumId, @Param("keyword") String keyword,
                   @Param("favorite") Boolean favorite, @Param("tagId") Long tagId);

    Long getTotalFileSizeByUserId(@Param("userId") Long userId);

    int countMediaByUserId(@Param("userId") Long userId);

    int insert(Media media);

    int updateFavorite(@Param("id") Long id, @Param("isFavorite") boolean isFavorite);

    int updateDeleted(@Param("id") Long id, @Param("isDeleted") boolean isDeleted,
                      @Param("deletedAt") LocalDateTime deletedAt);

    int restore(@Param("id") Long id);

    int updateAlbum(@Param("id") Long id, @Param("albumId") Long albumId);

    int deleteById(@Param("id") Long id);

    int incrementViewCount(@Param("id") Long id);

    List<Media> findExpiredDeleted(@Param("days") int days);

    int updateVideoMp4Path(@Param("id") Long id, @Param("videoMp4Path") String videoMp4Path);

    int updateCoverAndDuration(@Param("id") Long id, @Param("coverImage") String coverImage,
                               @Param("duration") Integer duration);

    int batchUpdateTitle(@Param("ids") List<Long> ids, @Param("userId") Long userId,
                         @Param("title") String title, @Param("description") String description);

    List<Media> findByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    List<Media> findPublic(@Param("offset") int offset, @Param("size") int size);

    long countPublic();

    /** 随机取公开媒体（含作者昵称/头像），供公开广场使用 */
    List<MediaVO> findRandomPublic(@Param("limit") int limit);

    /** 相册内全部媒体按手动排序（分享页等无属主场景） */
    List<Media> findByAlbumId(@Param("albumId") Long albumId);

    int updateIsPublic(@Param("id") Long id, @Param("isPublic") boolean isPublic);

    int renameTitle(@Param("id") Long id,@Param("userId") Long userId,@Param("title") String title);

    int updateSortOrder(@Param("id") Long id, @Param("userId") Long userId,
                        @Param("sortOrder") int sortOrder);
}
