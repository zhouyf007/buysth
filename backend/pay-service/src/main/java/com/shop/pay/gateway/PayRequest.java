package com.shop.pay.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    private String payNo;
    private String orderNo;
    private String subject;
    private BigDecimal amount;
}

