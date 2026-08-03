package com.shop.order.controller;

import com.shop.api.dto.OrderInfoDTO;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.common.result.Result;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PostMapping("/seckill")
    public Result<OrderInfoDTO> createSeckillOrder(@RequestBody SeckillOrderRequest request) {
        return Result.ok(orderService.createSeckillOrder(request));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderInfoDTO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.internalByOrderNo(orderNo));
    }
}

