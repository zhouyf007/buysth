package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long categoryId;
    private String name;
    private String subtitle;
    private String brand;
    private String region;
    private String mainImage;
    private String detail;
    private LocalDateTime publishDate;
    private Integer status;
    private Integer sales;
    private BigDecimal rating;
}

