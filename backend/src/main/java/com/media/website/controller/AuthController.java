package com.media.website.controller;

import com.media.website.entity.User;
import com.media.website.repository.UserRepository;
import com.media.website.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }

        User user = new User(username, password, "USER");
        userRepository.save(user);

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return ResponseEntity.ok(Map.of(
            "message", "注册成功",
            "token", token,
            "user", Map.of("id", user.getId(), "username", username, "role", user.getRole())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名或密码错误"));
        }

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return ResponseEntity.ok(Map.of(
            "message", "登录成功",
            "token", token,
            "user", Map.of("id", user.getId(), "username", username, "role", user.getRole())
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "未登录"));
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "token无效"));
        }

        String username = jwtService.getUsernameFromToken(token);
        Long userId = jwtService.getUserIdFromToken(token);

        return ResponseEntity.ok(Map.of(
            "id", userId,
            "username", username
        ));
    }
}