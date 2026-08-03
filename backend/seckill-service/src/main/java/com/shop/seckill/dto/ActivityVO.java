package com.shop.seckill.dto;

import com.shop.seckill.entity.SeckillProduct;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityVO {

    private Long id;
    private String name;
    private String type;
    private String promotionCode;
    private String discountType;
    private BigDecimal discountValue;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private List<ProductVO> products;

    @Data
    public static class ProductVO {
        private Long id;
        private Long skuId;
        private Long productId;
        private String productName;
        private String skuSpec;
        private String image;
        private BigDecimal seckillPrice;
        private Integer seckillStock;
        private Integer remainStock;
        private Integer limitPerUser;
    }
}

