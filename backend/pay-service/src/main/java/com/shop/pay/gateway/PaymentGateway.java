package com.shop.pay.gateway;

public interface PaymentGateway {

    String channel();

    PayCreateResponse create(PayRequest request);
}

