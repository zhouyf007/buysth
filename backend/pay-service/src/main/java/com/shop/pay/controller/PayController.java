package com.shop.pay.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.Result;
import com.shop.pay.entity.Payment;
import com.shop.pay.gateway.PayCreateResponse;
import com.shop.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/create")
    public Result<PayCreateResponse> create(@RequestBody Map<String, String> body) {
        return Result.ok(payService.createPayment(UserContext.getUserId(), body.get("orderNo")));
    }

    @GetMapping("/status/{orderNo}")
    public Result<Payment> status(@PathVariable String orderNo) {
        return Result.ok(payService.status(orderNo));
    }

    @GetMapping(value = "/mock/{payNo}", produces = MediaType.TEXT_HTML_VALUE)
    public byte[] mockPage(@PathVariable String payNo,
                           @RequestParam(required = false) String redirect) {
        return payService.mockPayPage(payNo, redirect).getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/mock/notify/{payNo}")
    public Result<Void> mockNotify(@PathVariable String payNo) {
        payService.mockNotify(payNo);
        return Result.ok();
    }
}
