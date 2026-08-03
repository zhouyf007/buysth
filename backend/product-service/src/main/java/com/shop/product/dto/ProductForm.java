package com.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductForm {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;
    private String brand;
    private String region;
    private String mainImage;
    private String detail;
    private LocalDateTime publishDate;
    private Integer status;
    private List<SkuForm> skus;

    @Data
    public static class SkuForm {
        private String specName;
        private String specValue;
        private BigDecimal price;
        private Integer stock;
        private String image;
        private Integer status;
    }
}

