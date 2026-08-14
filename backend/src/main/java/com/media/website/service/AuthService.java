package com.media.website.service;

import com.media.website.dto.LoginRequestDto;
import com.media.website.dto.RegisterRequestDto;
import com.media.website.entity.User;
import com.media.website.mapper.UserMapper;
import com.media.website.vo.AuthResponseVO;
import com.media.website.vo.MeVO;
import com.media.website.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthResponseVO register(RegisterRequestDto request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (userMapper.countByUsername(username) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User(username, password, "USER");
        userMapper.insert(user);

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return AuthResponseVO.of("注册成功", token, UserVO.from(user));
    }

    public AuthResponseVO login(LoginRequestDto request) {
        String username = request.getUsername();
        String password = request.getPassword();

        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return AuthResponseVO.of("登录成功", token, UserVO.from(user));
    }

    public MeVO getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录");
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("token无效");
        }
        MeVO me = new MeVO();
        me.setId(jwtService.getUserIdFromToken(token));
        me.setUsername(jwtService.getUsernameFromToken(token));
        return me;
    }
}
