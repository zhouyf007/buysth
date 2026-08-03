package com.shop.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_record")
public class SeckillRecord extends BaseEntity {

    private Long activityId;
    private Long seckillProductId;
    private Long skuId;
    private Long userId;
    private String orderNo;
    private Integer quantity;
    private String status;
}

