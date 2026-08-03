package com.shop.seckill.controller;

import com.shop.api.dto.PromotionDTO;
import com.shop.common.result.Result;
import com.shop.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/promotions")
@RequiredArgsConstructor
public class InternalSeckillController {

    private final SeckillService seckillService;

    @GetMapping("/{code}")
    public Result<PromotionDTO> promotion(@PathVariable String code) {
        return Result.ok(seckillService.internalPromotion(code));
    }
}

