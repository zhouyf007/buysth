package com.shop.seckill.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.seckill.dto.ActivityVO;
import com.shop.seckill.dto.SeckillResult;
import com.shop.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @GetMapping("/activities")
    public Result<PageResult<ActivityVO>> activities(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String type) {
        return Result.ok(seckillService.listActivities(current, size, type));
    }

    @GetMapping("/activities/{id}")
    public Result<ActivityVO> detail(@PathVariable Long id) {
        return Result.ok(seckillService.detail(id));
    }

    @PostMapping("/{activityId}/products/{productId}")
    public Result<SeckillResult> seckill(@PathVariable Long activityId, @PathVariable Long productId) {
        return Result.ok(seckillService.seckill(UserContext.getUserId(), activityId, productId));
    }
}

