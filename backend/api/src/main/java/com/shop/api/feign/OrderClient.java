package com.shop.api.feign;

import com.shop.api.dto.OrderInfoDTO;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/internal/orders/seckill")
    Result<OrderInfoDTO> createSeckillOrder(@RequestBody SeckillOrderRequest request);

    @GetMapping("/internal/orders/{orderNo}")
    Result<OrderInfoDTO> getByOrderNo(@PathVariable("orderNo") String orderNo);
}

