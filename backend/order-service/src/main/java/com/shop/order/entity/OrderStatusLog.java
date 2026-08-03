package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_status_log")
public class OrderStatusLog extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private String fromStatus;
    private String toStatus;
    private String operator;
    private String remark;
}

