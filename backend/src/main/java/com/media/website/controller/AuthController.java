package com.media.website.controller;

import com.media.website.dto.LoginRequestDto;
import com.media.website.dto.PasswordChangeRequestDto;
import com.media.website.dto.RegisterRequestDto;
import com.media.website.service.AuthService;
import com.media.website.vo.ApiMessageVO;
import com.media.website.vo.ApiErrorVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(authService.getCurrentUser(authHeader));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequestDto request,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            authService.changePassword(authHeader, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ApiMessageVO("密码修改成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }
}
