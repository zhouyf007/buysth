package com.shop.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderResult {

    private String orderNo;
    private BigDecimal payAmount;
}

