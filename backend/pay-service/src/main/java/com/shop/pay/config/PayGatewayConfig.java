package com.shop.pay.config;

import com.shop.common.exception.BizException;
import com.shop.pay.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PayGatewayConfig {

    private final List<PaymentGateway> gateways;

    @Value("${shop.pay.channel:mock}")
    private String channel;

    public PaymentGateway activeGateway() {
        return gateways.stream()
                .filter(g -> g.channel().equalsIgnoreCase(channel))
                .findFirst()
                .orElseThrow(() -> new BizException("未配置可用的支付网关: " + channel));
    }
}

