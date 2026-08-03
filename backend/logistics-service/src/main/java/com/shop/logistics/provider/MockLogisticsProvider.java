package com.shop.logistics.provider;

import com.shop.common.util.OrderNoGenerator;
import org.springframework.stereotype.Component;

@Component
public class MockLogisticsProvider implements LogisticsProvider {

    @Override
    public String companyCode() {
        return "SF";
    }

    @Override
    public String companyName() {
        return "顺丰速运";
    }

    @Override
    public String createTrackingNo(String shipmentNo) {
        return "SF" + OrderNoGenerator.generate("");
    }
}

