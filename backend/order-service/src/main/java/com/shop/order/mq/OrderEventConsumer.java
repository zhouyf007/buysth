package com.shop.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.common.mq.MQConstants;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MQConstants.Q_ORDER_SECKILL)
    public void onSeckillOrder(String message) {
        try {
            SeckillOrderRequest request = objectMapper.readValue(message, SeckillOrderRequest.class);
            orderService.createSeckillOrder(request);
        } catch (Exception e) {
            log.error("handle seckill order failed: {}", message, e);
        }
    }

    @RabbitListener(queues = MQConstants.Q_ORDER_PAID)
    public void onPaid(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            orderService.markPaid(String.valueOf(event.get("orderNo")),
                    String.valueOf(event.get("channel")),
                    event.get("paidTime") == null ? LocalDateTime.now()
                            : LocalDateTime.parse(String.valueOf(event.get("paidTime"))));
        } catch (Exception e) {
            log.error("handle paid event failed: {}", message, e);
        }
    }

    @RabbitListener(queues = MQConstants.Q_ORDER_TIMEOUT)
    public void onTimeout(String orderNo) {
        try {
            orderService.adminCancel(orderNo, "system");
        } catch (Exception e) {
            log.warn("cancel timeout order failed, orderNo={}", orderNo, e.getMessage());
        }
    }

    @RabbitListener(queues = MQConstants.Q_ORDER_SHIPPED)
    public void onShipped(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            orderService.markShipped(String.valueOf(event.get("orderNo")),
                    String.valueOf(event.get("shipmentNo")));
        } catch (Exception e) {
            log.error("handle shipped event failed: {}", message, e);
        }
    }
}

