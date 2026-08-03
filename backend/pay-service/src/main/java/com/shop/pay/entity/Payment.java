package com.shop.pay.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {

    private String payNo;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String channel;
    private String subject;
    private String status;
    private String notifyData;
    private LocalDateTime paidTime;
}

