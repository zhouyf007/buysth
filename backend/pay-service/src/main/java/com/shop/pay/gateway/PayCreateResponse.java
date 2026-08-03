package com.shop.pay.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayCreateResponse {

    private String channel;
    private String payUrl;
    private String qrCode;
    private String message;
}

