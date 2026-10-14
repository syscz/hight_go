package com.media.website.service;

import com.media.website.entity.User;
import com.media.website.mapper.UserMapper;
import com.media.website.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;

    public UserVO updateAvatar(Long userId, MultipartFile file) throws IOException {
        String url = saveImage(file, 320);
        userMapper.updateAvatar(userId, url);
        return UserVO.from(requireUser(userId));
    }

    public UserVO updateAvatarBg(Long userId, MultipartFile file) throws IOException {
        String url = saveImage(file, 1600);
        userMapper.updateAvatarBg(userId, url);
        return UserVO.from(requireUser(userId));
    }

    private String saveImage(MultipartFile file, int thumbWidth) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String fileType = fileUploadService.getFileType(file.getOriginalFilename());
        if (!"image".equals(fileType)) {
            throw new IllegalArgumentException("只支持图片文件");
        }
        String url = fileUploadService.uploadFile(file);
        return fileUploadService.generateThumbnail(url, thumbWidth);
    }

    private User requireUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
}
