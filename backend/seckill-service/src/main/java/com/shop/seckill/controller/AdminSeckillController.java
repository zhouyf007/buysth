package com.shop.seckill.controller;

import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.seckill.dto.ActivityForm;
import com.shop.seckill.dto.ActivityVO;
import com.shop.seckill.entity.SeckillProduct;
import com.shop.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSeckillController {

    private final SeckillService seckillService;

    @GetMapping("/activities")
    public Result<PageResult<ActivityVO>> activities(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(required = false) String status) {
        List<ActivityVO> list = seckillService.adminActivities(current, size, type, status);
        return Result.ok(PageResult.of(list, list.size(), current, size));
    }

    @GetMapping("/activities/{id}")
    public Result<ActivityVO> detail(@PathVariable Long id) {
        return Result.ok(seckillService.adminDetail(id));
    }

    @PostMapping("/activities")
    public Result<Void> create(@Valid @RequestBody ActivityForm form) {
        seckillService.createActivity(form);
        return Result.ok();
    }

    @PutMapping("/activities/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityForm form) {
        seckillService.updateActivity(id, form);
        return Result.ok();
    }

    @DeleteMapping("/activities/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        seckillService.deleteActivity(id);
        return Result.ok();
    }

    @PutMapping("/activities/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        seckillService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PostMapping("/activities/{id}/preload")
    public Result<Void> preload(@PathVariable Long id) {
        seckillService.preload(id);
        return Result.ok();
    }

    @GetMapping("/seckill-products")
    public Result<List<SeckillProduct>> products(@RequestParam(required = false) Long activityId) {
        return Result.ok(seckillService.adminProducts(activityId));
    }
}

