package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long skuId;
    private Long productId;
    private String productName;
    private String skuSpec;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}

