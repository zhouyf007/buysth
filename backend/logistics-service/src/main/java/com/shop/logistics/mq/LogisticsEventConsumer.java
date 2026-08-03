package com.shop.logistics.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.exception.BizException;
import com.shop.common.mq.MQConstants;
import com.shop.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsEventConsumer {

    private final LogisticsService logisticsService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MQConstants.Q_LOGISTICS_ORDER_PAID)
    public void onOrderPaid(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderNo = String.valueOf(event.get("orderNo"));
            for (int attempt = 0; attempt < 6; attempt++) {
                try {
                    logisticsService.createShipment(orderNo, "system");
                    return;
                } catch (BizException e) {
                    if (attempt == 5) {
                        throw e;
                    }
                    Thread.sleep(3000);
                }
            }
        } catch (Exception e) {
            log.error("handle order paid failed: {}", message, e);
        }
    }
}
