package com.shop.product.dto;

import lombok.Data;

@Data
public class CategorySalesStat {

    private Long categoryId;
    private String categoryName;
    private Integer sales;
    private Integer productCount;
}

