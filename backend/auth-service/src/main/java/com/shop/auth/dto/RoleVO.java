package com.shop.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private List<Long> menuIds;
}

