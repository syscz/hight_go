package com.media.website.service;

import com.media.website.dto.LoginRequestDto;
import com.media.website.dto.RegisterRequestDto;
import com.media.website.entity.User;
import com.media.website.mapper.UserMapper;
import com.media.website.vo.AuthResponseVO;
import com.media.website.vo.MeVO;
import com.media.website.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 10 * 60 * 1000L;

    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 注册邀请码：为空 = 关闭注册；配置后注册必须携带匹配的邀请码 */
    @Value("${auth.invite-code:}")
    private String inviteCode;

    /** 登录失败计数（内存版，单机部署够用）：连续失败 5 次锁定 10 分钟 */
    private final Map<String, LoginFail> loginFails = new ConcurrentHashMap<>();

    private static class LoginFail {
        int count;
        long lockedUntil;
    }

    private boolean isBcrypt(String password) {
        return password != null && password.startsWith("$2");
    }

    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        // 兼容旧明文密码（登录成功后自动升级为 BCrypt）
        return storedPassword != null && storedPassword.equals(rawPassword);
    }

    public AuthResponseVO register(RegisterRequestDto request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("注册已关闭，请联系管理员开通账号");
        }
        if (request.getInviteCode() == null || request.getInviteCode().isBlank()) {
            throw new IllegalArgumentException("请填写邀请码");
        }
        if (!inviteCode.equals(request.getInviteCode())) {
            throw new IllegalArgumentException("邀请码不正确");
        }
        if (userMapper.countByUsername(username) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User(username, passwordEncoder.encode(password), "USER");
        userMapper.insert(user);

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return AuthResponseVO.of("注册成功", token, UserVO.from(user));
    }

    public AuthResponseVO login(LoginRequestDto request) {
        String username = request.getUsername();
        String password = request.getPassword();

        checkLocked(username);
        User user = userMapper.findByUsername(username);
        if (user == null || !verifyPassword(password, user.getPassword())) {
            recordLoginFail(username);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        loginFails.remove(username);

        if (!isBcrypt(user.getPassword())) {
            userMapper.updatePassword(user.getId(), passwordEncoder.encode(password));
            user.setPassword(userMapper.findById(user.getId()).getPassword());
        }

        String token = jwtService.generateToken(username, user.getId(), user.getRole());
        return AuthResponseVO.of("登录成功", token, UserVO.from(user));
    }

    private void checkLocked(String username) {
        LoginFail fail = loginFails.get(username);
        if (fail != null && fail.lockedUntil > System.currentTimeMillis()) {
            long minutes = (fail.lockedUntil - System.currentTimeMillis()) / 60000 + 1;
            throw new IllegalArgumentException("失败次数过多，账号已临时锁定，请约 " + minutes + " 分钟后再试");
        }
    }

    private void recordLoginFail(String username) {
        LoginFail fail = loginFails.computeIfAbsent(username, k -> new LoginFail());
        fail.count++;
        if (fail.count >= MAX_LOGIN_ATTEMPTS) {
            fail.lockedUntil = System.currentTimeMillis() + LOCK_MILLIS;
            fail.count = 0;
            log.warn("登录连续失败 {} 次，临时锁定用户: {}", MAX_LOGIN_ATTEMPTS, username);
        }
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
        User user = userMapper.findById(me.getId());
        if (user != null) {
            me.setAvatar(user.getAvatar());
            me.setAvatarBg(user.getAvatarBg());
        }
        return me;
    }

    public void changePassword(String authHeader, String oldPassword, String newPassword) {
        String token = extractToken(authHeader);
        Long userId = jwtService.getUserIdFromToken(token);
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录");
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("token无效");
        }
        return token;
    }
}
