package com.shop.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer status;
    private List<String> roles;
    private List<String> permissions;
}

