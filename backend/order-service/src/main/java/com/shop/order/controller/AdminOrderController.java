package com.shop.order.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.order.dto.OrderVO;
import com.shop.order.dto.MonthlyOrderStat;
import com.shop.order.dto.StatusStat;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long userId) {
        return Result.ok(orderService.adminPage(current, size, keyword, status, userId));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detail(null, orderNo, true));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.adminCancel(orderNo, UserContext.getUsername() == null ? "admin" : UserContext.getUsername());
        return Result.ok();
    }

    @GetMapping("/monthly-stats")
    public Result<List<MonthlyOrderStat>> monthlyStats() {
        return Result.ok(orderService.monthlyStats());
    }

    @GetMapping("/status-stats")
    public Result<List<StatusStat>> statusStats() {
        return Result.ok(orderService.statusStats());
    }

    @DeleteMapping("/{orderNo}")
    public Result<Void> deleteOrder(@PathVariable String orderNo) {
        orderService.adminDeleteOrders(List.of(orderNo),
                UserContext.getUsername() == null ? "admin" : UserContext.getUsername());
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody Map<String, List<String>> body) {
        orderService.adminDeleteOrders(body.get("orderNos"),
                UserContext.getUsername() == null ? "admin" : UserContext.getUsername());
        return Result.ok();
    }
}
