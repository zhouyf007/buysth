package com.shop.seckill.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityForm {

    @NotBlank(message = "活动名称不能为空")
    private String name;

    @NotBlank(message = "活动类型不能为空")
    private String type;

    private String promotionCode;
    private String discountType;
    private BigDecimal discountValue;
    private String description;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String status;
    private List<ProductForm> products;

    @Data
    public static class ProductForm {
        private Long skuId;
        private BigDecimal seckillPrice;
        private Integer seckillStock;
        private Integer limitPerUser;
        private Integer status;
    }
}

