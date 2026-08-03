package com.shop.auth.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuVO {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String icon;
    private String permissionCode;
    private String type;
    private Integer sort;
    private List<MenuVO> children = new ArrayList<>();
}

