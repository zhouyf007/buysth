package com.shop.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String brand;
    private String region;
    private String mainImage;
    private List<String> images;
    private BigDecimal minPrice;
    private Integer sales;
    private BigDecimal rating;
    private LocalDateTime publishDate;
    private Integer status;
    private String detail;
}

