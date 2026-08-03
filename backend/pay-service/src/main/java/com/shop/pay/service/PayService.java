package com.shop.pay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.OrderInfoDTO;
import com.shop.api.feign.OrderClient;
import com.shop.common.exception.BizException;
import com.shop.common.mq.MQConstants;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.common.util.OrderNoGenerator;
import com.shop.pay.config.PayGatewayConfig;
import com.shop.pay.entity.Payment;
import com.shop.pay.gateway.PayCreateResponse;
import com.shop.pay.gateway.PayRequest;
import com.shop.pay.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CLOSED = "CLOSED";

    private final PaymentMapper paymentMapper;
    private final PayGatewayConfig gatewayConfig;
    private final OrderClient orderClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public PayCreateResponse createPayment(Long userId, String orderNo) {
        OrderInfoDTO order = getOrder(orderNo);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(403, "无权支付该订单");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BizException("订单当前状态不可支付");
        }
        Payment exist = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .eq(Payment::getStatus, STATUS_PENDING));
        if (exist != null) {
            return new PayCreateResponse(exist.getChannel(),
                    "/api/pay/mock/" + exist.getPayNo(),
                    "mock-qr-" + exist.getPayNo(),
                    "支付单已存在");
        }
        String payNo = OrderNoGenerator.generate("P");
        Payment payment = new Payment();
        payment.setPayNo(payNo);
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setChannel(gatewayConfig.activeGateway().channel());
        payment.setSubject(order.getTitle() == null ? "数码商城订单" : order.getTitle());
        payment.setStatus(STATUS_PENDING);
        PayCreateResponse response = gatewayConfig.activeGateway()
                .create(new PayRequest(payNo, orderNo, payment.getSubject(), payment.getAmount()));
        paymentMapper.insert(payment);
        return response;
    }

    public Payment status(String orderNo) {
        return paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1"));
    }

    public String mockPayPage(String payNo, String redirect) {
        Payment payment = getByPayNo(payNo);
        if (!STATUS_PENDING.equals(payment.getStatus())) {
            return "<!DOCTYPE html><html lang='zh-CN'><head><meta charset='UTF-8'><title>模拟支付</title></head><body>"
                    + "<h3>该支付单已处理，状态: " + payment.getStatus() + "</h3>"
                    + "<p><a href='" + safeRedirect(redirect) + "'>返回我的订单</a></p></body></html>";
        }
        String target = safeRedirect(redirect);
        return "<!DOCTYPE html><html lang='zh-CN'><head><meta charset='UTF-8'>"
                + "<title>模拟支付</title>"
                + "<style>body{font-family:system-ui;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;background:#f5f7fa}"
                + ".card{background:#fff;border-radius:8px;box-shadow:0 8px 24px rgba(0,0,0,.08);padding:40px 48px;text-align:center;min-width:320px}"
                + "h2{margin:0 0 8px}.amount{font-size:28px;font-weight:700;color:#ff6a00;margin:16px 0}"
                + "button{background:#ff6a00;color:#fff;border:0;border-radius:6px;padding:12px 32px;font-size:16px;cursor:pointer}"
                + "button:disabled{opacity:.6;cursor:not-allowed}"
                + ".msg{margin-top:16px;font-size:15px;color:#16a34a;min-height:22px}"
                + "</style></head><body><div class='card'>"
                + "<h2>模拟支付收银台</h2>"
                + "<div>订单号：" + payment.getOrderNo() + "</div>"
                + "<div class='amount'>¥" + payment.getAmount().toPlainString() + "</div>"
                + "<button id='payBtn' onclick='doPay()'>确认支付</button>"
                + "<div id='msg' class='msg'></div>"
                + "<p style='color:#999;font-size:12px'>当前为沙箱/模拟通道，不会产生真实扣款</p>"
                + "<script>"
                + "async function doPay(){"
                + "var btn=document.getElementById('payBtn');"
                + "btn.disabled=true;"
                + "try{"
                + "var res=await fetch('/api/pay/mock/notify/" + payNo + "',{method:'POST',headers:{'Content-Type':'application/json'},body:'{}'});"
                + "var data=await res.json();"
                + "if(data.code===0){document.getElementById('msg').textContent='支付成功，正在跳转到我的订单...';"
                + "setTimeout(function(){location.href='" + target + "';},1200);}"
                + "else{document.getElementById('msg').textContent=data.message||'支付失败';btn.disabled=false;}"
                + "}catch(e){document.getElementById('msg').textContent='支付失败，请重试';btn.disabled=false;}"
                + "}"
                + "</script>"
                + "</div></body></html>";
    }

    private String safeRedirect(String redirect) {
        if (redirect != null && (redirect.startsWith("http://localhost")
                || redirect.startsWith("http://127.0.0.1"))) {
            return redirect;
        }
        return "http://localhost:5173/orders";
    }

    @Transactional
    public void mockNotify(String payNo) {
        Payment payment = getByPayNo(payNo);
        if (!STATUS_PENDING.equals(payment.getStatus())) {
            return;
        }
        payment.setStatus(STATUS_SUCCESS);
        payment.setPaidTime(LocalDateTime.now());
        payment.setNotifyData("{\"mock\":true,\"payNo\":\"" + payNo + "\"}");
        paymentMapper.updateById(payment);
        publishPaid(payment);
    }

    public PageResult<Payment> adminPage(long current, long size, String keyword, String status) {
        Page<Payment> page = paymentMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Payment>()
                        .eq(status != null && !status.isBlank(), Payment::getStatus, status)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(Payment::getPayNo, keyword)
                                .or().like(Payment::getOrderNo, keyword))
                        .orderByDesc(Payment::getCreateTime));
        return PageResult.of(page);
    }

    private void publishPaid(Payment payment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "ORDER_PAID");
            event.put("orderNo", payment.getOrderNo());
            event.put("payNo", payment.getPayNo());
            event.put("channel", payment.getChannel());
            event.put("amount", payment.getAmount());
            event.put("paidTime", payment.getPaidTime().toString());
            rabbitTemplate.convertAndSend(MQConstants.TOPIC_EXCHANGE, MQConstants.KEY_ORDER_PAID,
                    objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("publish paid event failed, payNo={}", payment.getPayNo(), e);
        }
    }

    private Payment getByPayNo(String payNo) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>().eq(Payment::getPayNo, payNo));
        if (payment == null) {
            throw new BizException(404, "支付单不存在");
        }
        return payment;
    }

    private OrderInfoDTO getOrder(String orderNo) {
        try {
            Result<OrderInfoDTO> result = orderClient.getByOrderNo(orderNo);
            return result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            throw new BizException("订单服务暂时不可用");
        }
    }
}
