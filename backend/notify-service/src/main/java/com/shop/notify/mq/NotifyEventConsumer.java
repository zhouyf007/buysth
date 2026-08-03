package com.shop.notify.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.common.mq.MQConstants;
import com.shop.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyEventConsumer {

    private final NotifyService notifyService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MQConstants.Q_NOTIFY_SEND)
    public void onEvent(String message) {
        try {
            if (message.contains("seckillPrice")) {
                SeckillOrderRequest request = objectMapper.readValue(message, SeckillOrderRequest.class);
                notifyService.onSeckillSuccess(request);
                return;
            }
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String type = event.get("type") == null ? "" : String.valueOf(event.get("type"));
            switch (type) {
                case "ORDER_PAID" -> notifyService.onOrderPaid(event);
                case "ORDER_CANCELLED" -> notifyService.onOrderCancelled(event);
                case "ORDER_SHIPPED" -> notifyService.onOrderShipped(event);
                default -> log.warn("unknown notify event: {}", message);
            }
        } catch (Exception e) {
            log.error("handle notify event failed: {}", message, e);
        }
    }
}

