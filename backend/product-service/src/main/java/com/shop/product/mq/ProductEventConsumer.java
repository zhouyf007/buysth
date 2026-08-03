package com.shop.product.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.StockLockRequest;
import com.shop.common.mq.MQConstants;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MQConstants.Q_PRODUCT_ORDER_CANCELLED)
    public void onOrderCancelled(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderNo = String.valueOf(event.get("orderNo"));
            List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");
            if (items != null) {
                items.forEach(item -> productService.releaseStock(new StockLockRequest(
                        orderNo,
                        Long.valueOf(String.valueOf(item.get("skuId"))),
                        Integer.valueOf(String.valueOf(item.get("quantity"))),
                        null)));
            }
        } catch (Exception e) {
            log.error("handle order cancelled failed: {}", message, e);
        }
    }
}

