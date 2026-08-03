package com.shop.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionPreviewResponse {

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
}

