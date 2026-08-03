package com.shop.logistics.controller;

import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.logistics.dto.ShipmentVO;
import com.shop.logistics.dto.TrackRequest;
import com.shop.logistics.entity.Shipment;
import com.shop.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
public class AdminLogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping
    public Result<PageResult<ShipmentVO>> page(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String status) {
        return Result.ok(logisticsService.adminPage(current, size, keyword, status));
    }

    @PostMapping
    public Result<Shipment> create(@RequestBody Map<String, String> body) {
        return Result.ok(logisticsService.createShipment(body.get("orderNo"), "admin"));
    }

    @PostMapping("/{id}/track")
    public Result<Void> addTrack(@PathVariable Long id, @Valid @RequestBody TrackRequest request) {
        logisticsService.addTrack(id, request);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        logisticsService.updateStatus(id, body.get("status"));
        return Result.ok();
    }
}

