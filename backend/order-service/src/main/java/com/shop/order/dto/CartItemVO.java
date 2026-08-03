package com.shop.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long id;
    private Long skuId;
    private Long productId;
    private String productName;
    private String skuSpec;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer checked;
    private BigDecimal subtotal;
}

