package com.media.website.vo;

import lombok.Data;

@Data
public class AuthResponseVO {
    private String message;
    private String token;
    private UserVO user;

    public static AuthResponseVO of(String message, String token, UserVO user) {
        AuthResponseVO vo = new AuthResponseVO();
        vo.setMessage(message);
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }
}
