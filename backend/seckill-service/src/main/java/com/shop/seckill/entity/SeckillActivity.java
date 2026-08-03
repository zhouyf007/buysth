package com.shop.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_activity")
public class SeckillActivity extends BaseEntity {

    private String name;
    private String type;
    private String promotionCode;
    private String discountType;
    private BigDecimal discountValue;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}

