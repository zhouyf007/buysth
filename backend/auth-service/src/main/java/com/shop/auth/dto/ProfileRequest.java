package com.shop.auth.dto;

import lombok.Data;

@Data
public class ProfileRequest {

    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}

