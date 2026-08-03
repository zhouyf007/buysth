package com.shop.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyOrderStat {

    private String month;
    private Long orderCount;
    private BigDecimal totalAmount;
}

