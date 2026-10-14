package com.media.website.controller;

import com.media.website.service.UserService;
import com.media.website.util.AuthUtils;
import com.media.website.vo.ApiErrorVO;
import com.media.website.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthUtils authUtils;

    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(userService.updateAvatar(userId, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiErrorVO("上传失败"));
        }
    }

    @PostMapping("/avatar-bg")
    public ResponseEntity<?> updateAvatarBg(@RequestParam("file") MultipartFile file,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = authUtils.getUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(userService.updateAvatarBg(userId, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiErrorVO("上传失败"));
        }
    }
}
