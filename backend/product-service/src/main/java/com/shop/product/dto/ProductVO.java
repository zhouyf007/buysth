package com.shop.product.dto;

import com.shop.product.entity.ProductSku;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
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
    private BigDecimal minPrice;
    private Integer totalStock;
    private List<ProductSku> skus;
}

