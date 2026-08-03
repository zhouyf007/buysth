package com.shop.pay.gateway;

import org.springframework.stereotype.Component;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String channel() {
        return "mock";
    }

    @Override
    public PayCreateResponse create(PayRequest request) {
        return new PayCreateResponse("mock",
                "/api/pay/mock/" + request.getPayNo(),
                "mock-qr-" + request.getPayNo(),
                "模拟支付网关已创建支付单");
    }
}

