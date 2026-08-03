package com.shop.pay.gateway;

import com.shop.common.exception.BizException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "shop.pay.channel", havingValue = "alipay-sandbox")
public class AlipaySandboxGateway implements PaymentGateway {

    @Override
    public String channel() {
        return "alipay-sandbox";
    }

    @Override
    public PayCreateResponse create(PayRequest request) {
        throw new BizException("支付宝沙箱网关为预留适配点，请配置沙箱 AppID、私钥等参数后使用");
    }
}

