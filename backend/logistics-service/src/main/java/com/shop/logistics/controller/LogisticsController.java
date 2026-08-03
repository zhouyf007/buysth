package com.shop.logistics.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.Result;
import com.shop.logistics.dto.ShipmentVO;
import com.shop.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/track/{orderNo}")
    public Result<ShipmentVO> track(@PathVariable String orderNo) {
        return Result.ok(logisticsService.track(orderNo, UserContext.getUserId(), false));
    }
}

