package com.shop.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserVO user;
}

