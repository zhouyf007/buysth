package com.shop.pay.controller;

import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.pay.entity.Payment;
import com.shop.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPayController {

    private final PayService payService;

    @GetMapping
    public Result<PageResult<Payment>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status) {
        return Result.ok(payService.adminPage(current, size, keyword, status));
    }
}

