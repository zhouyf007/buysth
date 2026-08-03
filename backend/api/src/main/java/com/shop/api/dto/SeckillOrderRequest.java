package com.shop.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderRequest {

    private String orderNo;
    private Long userId;
    private Long activityId;
    private Long seckillProductId;
    private Long skuId;
    private Long productId;
    private String productName;
    private String skuSpec;
    private String image;
    private Integer quantity;
    private BigDecimal seckillPrice;
}

