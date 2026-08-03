package com.shop.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.OrderInfoDTO;
import com.shop.api.dto.PromotionDTO;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.api.dto.SkuDTO;
import com.shop.api.dto.StockLockRequest;
import com.shop.api.feign.ProductClient;
import com.shop.api.feign.SeckillClient;
import com.shop.common.exception.BizException;
import com.shop.common.mq.MQConstants;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.common.util.OrderNoGenerator;
import com.shop.order.dto.AddressDTO;
import com.shop.order.dto.CreateOrderResult;
import com.shop.order.dto.MonthlyOrderStat;
import com.shop.order.dto.OrderCreateRequest;
import com.shop.order.dto.OrderVO;
import com.shop.order.dto.StatusStat;
import com.shop.order.entity.OrderItem;
import com.shop.order.entity.OrderStatusLog;
import com.shop.order.entity.Orders;
import com.shop.order.mapper.OrderItemMapper;
import com.shop.order.mapper.OrderStatusLogMapper;
import com.shop.order.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    public static final String STATUS_PENDING_PAY = "PENDING_PAY";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_CLOSED = "CLOSED";

    private static final Map<String, String> STATUS_TEXT = Map.of(
            STATUS_PENDING_PAY, "待支付",
            STATUS_PAID, "已支付",
            STATUS_SHIPPED, "已发货",
            STATUS_COMPLETED, "已完成",
            STATUS_CANCELLED, "已取消",
            STATUS_CLOSED, "已关闭");

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final ProductClient productClient;
    private final SeckillClient seckillClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateOrderResult createOrder(Long userId, OrderCreateRequest request) {
        if (request.getAddress() == null || request.getAddress().getReceiverName() == null) {
            throw new BizException("请填写收货信息");
        }
        List<OrderLine> lines = buildLines(request.getItems());
        BigDecimal total = lines.stream().map(line -> line.subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.getPromotionCode())) {
            discount = applyPromotion(request.getPromotionCode(), total);
        }
        BigDecimal payAmount = total.subtract(discount).max(BigDecimal.ZERO);
        String orderNo = OrderNoGenerator.generate("O");
        Orders order = newOrder(orderNo, userId, "NORMAL", null, total, discount, payAmount,
                request.getAddress(), request.getRemark());
        ordersMapper.insert(order);
        saveItems(order.getId(), orderNo, lines);
        logStatus(order.getId(), orderNo, null, STATUS_PENDING_PAY, String.valueOf(userId), "提交订单");
        lockStock(orderNo, lines);
        try {
            sendTimeoutMessage(orderNo);
        } catch (Exception e) {
            releaseLocked(orderNo, lines);
            throw new BizException("下单服务暂时不可用");
        }
        CreateOrderResult result = new CreateOrderResult();
        result.setOrderNo(orderNo);
        result.setPayAmount(payAmount);
        return result;
    }

    @Transactional
    public OrderInfoDTO createSeckillOrder(SeckillOrderRequest request) {
        List<OrderLine> lines = List.of(new OrderLine(
                request.getSkuId(), request.getProductId(), request.getProductName(),
                request.getSkuSpec(), request.getImage(), request.getSeckillPrice(), request.getQuantity()));
        BigDecimal total = request.getSeckillPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        String orderNo = request.getOrderNo();
        Orders order = newOrder(orderNo, request.getUserId(), "SECKILL", request.getActivityId(),
                total, BigDecimal.ZERO, total, null, "秒杀订单");
        ordersMapper.insert(order);
        saveItems(order.getId(), orderNo, lines);
        logStatus(order.getId(), orderNo, null, STATUS_PENDING_PAY, String.valueOf(request.getUserId()), "秒杀下单成功");
        lockStock(orderNo, lines);
        try {
            sendTimeoutMessage(orderNo);
        } catch (Exception e) {
            releaseLocked(orderNo, lines);
            throw new BizException("秒杀下单服务暂时不可用");
        }
        return toOrderInfo(order);
    }

    @Transactional
    public void markPaid(String orderNo, String channel, LocalDateTime paidTime) {
        Orders order = getByOrderNo(orderNo);
        if (STATUS_PENDING_PAY.equals(order.getStatus())) {
            order.setStatus(STATUS_PAID);
            order.setPayChannel(channel);
            order.setPayTime(paidTime == null ? LocalDateTime.now() : paidTime);
            ordersMapper.updateById(order);
            logStatus(order.getId(), orderNo, STATUS_PENDING_PAY, STATUS_PAID, "system", "支付成功");
        }
    }

    @Transactional
    public void cancel(Long userId, String orderNo) {
        Orders order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该订单");
        }
        doCancel(order, String.valueOf(userId), "用户取消");
    }

    @Transactional
    public void adminCancel(String orderNo, String operator) {
        doCancel(getByOrderNo(orderNo), operator, "管理员取消");
    }

    @Transactional
    public void confirmReceipt(Long userId, String orderNo) {
        Orders order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该订单");
        }
        if (!STATUS_PAID.equals(order.getStatus()) && !STATUS_SHIPPED.equals(order.getStatus())) {
            throw new BizException("当前状态不可确认收货");
        }
        order.setStatus(STATUS_COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        logStatus(order.getId(), orderNo, order.getStatus(), STATUS_COMPLETED, String.valueOf(userId), "确认收货");
    }

    @Transactional
    public void markShipped(String orderNo, String shipmentNo) {
        Orders order = getByOrderNo(orderNo);
        if (STATUS_PAID.equals(order.getStatus())) {
            order.setStatus(STATUS_SHIPPED);
            order.setShipTime(LocalDateTime.now());
            ordersMapper.updateById(order);
            logStatus(order.getId(), orderNo, STATUS_PAID, STATUS_SHIPPED, "logistics", "运单 " + shipmentNo);
        }
    }

    @Transactional
    public void updateAddress(Long userId, String orderNo, AddressDTO address) {
        Orders order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该订单");
        }
        if (!STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new BizException("订单已支付，不能修改收货信息");
        }
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getReceiverAddress());
        ordersMapper.updateById(order);
    }

    public PageResult<OrderVO> userPage(Long userId, long current, long size, String status) {
        Page<Orders> page = ordersMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .eq(StringUtils.hasText(status), Orders::getStatus, status)
                        .orderByDesc(Orders::getCreateTime));
        return PageResult.of(toVOs(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public PageResult<OrderVO> adminPage(long current, long size, String keyword, String status, Long userId) {
        Page<Orders> page = ordersMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Orders>()
                        .eq(userId != null, Orders::getUserId, userId)
                        .eq(StringUtils.hasText(status), Orders::getStatus, status)
                        .and(StringUtils.hasText(keyword), w -> w.like(Orders::getOrderNo, keyword)
                                .or().like(Orders::getReceiverName, keyword)
                                .or().like(Orders::getReceiverPhone, keyword))
                        .orderByDesc(Orders::getCreateTime));
        return PageResult.of(toVOs(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public OrderVO detail(Long userId, String orderNo, boolean admin) {
        Orders order = getByOrderNo(orderNo);
        if (!admin && !order.getUserId().equals(userId)) {
            throw new BizException(403, "无权查看该订单");
        }
        return toVO(order);
    }

    public OrderInfoDTO internalByOrderNo(String orderNo) {
        Orders order = getByOrderNo(orderNo);
        return toOrderInfo(order);
    }

    public List<MonthlyOrderStat> monthlyStats() {
        return ordersMapper.monthlyStats();
    }

    public List<StatusStat> statusStats() {
        return ordersMapper.statusStats();
    }

    private void doCancel(Orders order, String operator, String remark) {
        if (!STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new BizException("只有待支付订单可以取消");
        }
        order.setStatus(STATUS_CANCELLED);
        ordersMapper.updateById(order);
        logStatus(order.getId(), order.getOrderNo(), STATUS_PENDING_PAY, STATUS_CANCELLED, operator, remark);
        publishCancelled(order);
    }

    private void publishCancelled(Orders order) {
        try {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderNo, order.getOrderNo()));
            List<Map<String, Object>> lines = items.stream().map(item -> {
                Map<String, Object> line = new HashMap<>();
                line.put("skuId", item.getSkuId());
                line.put("quantity", item.getQuantity());
                return line;
            }).collect(Collectors.toList());
            Map<String, Object> event = new HashMap<>();
            event.put("type", "ORDER_CANCELLED");
            event.put("orderNo", order.getOrderNo());
            event.put("items", lines);
            rabbitTemplate.convertAndSend(MQConstants.TOPIC_EXCHANGE, MQConstants.KEY_ORDER_CANCELLED,
                    objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("publish order cancelled failed, orderNo={}", order.getOrderNo(), e);
        }
    }

    private void sendTimeoutMessage(String orderNo) {
        rabbitTemplate.convertAndSend(MQConstants.DIRECT_EXCHANGE, MQConstants.Q_ORDER_TIMEOUT_TTL, orderNo);
    }

    private BigDecimal applyPromotion(String code, BigDecimal total) {
        try {
            Result<PromotionDTO> result = seckillClient.getPromotion(code);
            if (!result.isSuccess() || result.getData() == null) {
                throw new BizException("优惠活动不存在");
            }
            PromotionDTO promotion = result.getData();
            LocalDateTime now = LocalDateTime.now();
            if (promotion.getStartTime() != null && now.isBefore(promotion.getStartTime())
                    || promotion.getEndTime() != null && now.isAfter(promotion.getEndTime())) {
                throw new BizException("优惠活动未开始或已结束");
            }
            if ("PERCENT".equals(promotion.getDiscountType())) {
                return total.multiply(BigDecimal.ONE.subtract(
                        promotion.getDiscountValue().divide(BigDecimal.valueOf(100)))).setScale(2);
            }
            if ("FIXED".equals(promotion.getDiscountType())) {
                return promotion.getDiscountValue().min(total).setScale(2);
            }
            return BigDecimal.ZERO;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("营销服务暂时不可用");
        }
    }

    private List<OrderLine> buildLines(List<OrderCreateRequest.Item> items) {
        List<OrderLine> lines = new ArrayList<>();
        for (OrderCreateRequest.Item item : items) {
            SkuDTO sku = getSku(item.getSkuId());
            if (sku.getStock() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BizException("商品数量不合法");
            }
            if (item.getQuantity() > sku.getStock()) {
                throw new BizException("商品库存不足");
            }
            ProductNameHolder name = new ProductNameHolder("商品");
            try {
                var productResult = productClient.getProduct(sku.getProductId());
                if (productResult.isSuccess() && productResult.getData() != null) {
                    name.value = productResult.getData().getName();
                }
            } catch (Exception ignored) {
                // 名称回退
            }
            lines.add(new OrderLine(sku.getId(), sku.getProductId(), name.value,
                    sku.getSpecName() + " " + sku.getSpecValue(), sku.getImage(), sku.getPrice(), item.getQuantity()));
        }
        return lines;
    }

    private void lockStock(String orderNo, List<OrderLine> lines) {
        List<OrderLine> locked = new ArrayList<>();
        try {
            for (OrderLine line : lines) {
                StockLockRequest request = new StockLockRequest(orderNo, line.skuId, line.quantity, null);
                Result<Boolean> result = productClient.lockStock(request);
                if (!result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
                    throw new BizException("商品库存不足");
                }
                locked.add(line);
            }
        } catch (BizException e) {
            releaseLocked(orderNo, locked);
            throw e;
        } catch (Exception e) {
            releaseLocked(orderNo, locked);
            throw new BizException("商品服务暂时不可用");
        }
    }

    private void releaseLocked(String orderNo, List<OrderLine> lines) {
        for (OrderLine line : lines) {
            try {
                productClient.releaseStock(new StockLockRequest(orderNo, line.skuId, line.quantity, null));
            } catch (Exception ignored) {
                // 补偿失败记录日志即可
            }
        }
    }

    private SkuDTO getSku(Long skuId) {
        try {
            Result<SkuDTO> result = productClient.getSku(skuId);
            if (!result.isSuccess() || result.getData() == null) {
                throw new BizException(404, "商品不存在");
            }
            return result.getData();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("商品服务暂时不可用");
        }
    }

    private Orders newOrder(String orderNo, Long userId, String orderType, Long activityId,
                            BigDecimal total, BigDecimal discount, BigDecimal payAmount,
                            AddressDTO address, String remark) {
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setActivityId(activityId);
        order.setTotalAmount(total);
        order.setDiscountAmount(discount);
        order.setPayAmount(payAmount);
        order.setStatus(STATUS_PENDING_PAY);
        order.setReceiverName(address == null ? null : address.getReceiverName());
        order.setReceiverPhone(address == null ? null : address.getReceiverPhone());
        order.setReceiverAddress(address == null ? null : address.getReceiverAddress());
        order.setRemark(remark);
        return order;
    }

    private void saveItems(Long orderId, String orderNo, List<OrderLine> lines) {
        lines.forEach(line -> {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setOrderNo(orderNo);
            item.setSkuId(line.skuId);
            item.setProductId(line.productId);
            item.setProductName(line.productName);
            item.setSkuSpec(line.skuSpec);
            item.setImage(line.image);
            item.setPrice(line.price);
            item.setQuantity(line.quantity);
            item.setSubtotal(line.subtotal);
            orderItemMapper.insert(item);
        });
    }

    private void logStatus(Long orderId, String orderNo, String from, String to, String operator, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperator(operator);
        log.setRemark(remark);
        statusLogMapper.insert(log);
    }

    private Orders getByOrderNo(String orderNo) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        return order;
    }

    private List<OrderVO> toVOs(List<Orders> orders) {
        return orders.stream().map(this::toVO).collect(Collectors.toList());
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setOrderType(order.getOrderType());
        vo.setActivityId(order.getActivityId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(STATUS_TEXT.getOrDefault(order.getStatus(), order.getStatus()));
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setPayChannel(order.getPayChannel());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setItems(orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNo, order.getOrderNo())));
        return vo;
    }

    private OrderInfoDTO toOrderInfo(Orders order) {
        OrderInfoDTO dto = new OrderInfoDTO();
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setPayAmount(order.getPayAmount());
        dto.setStatus(order.getStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setCreateTime(order.getCreateTime());
        return dto;
    }

    private static class OrderLine {
        final Long skuId;
        final Long productId;
        final String productName;
        final String skuSpec;
        final String image;
        final BigDecimal price;
        final Integer quantity;
        final BigDecimal subtotal;

        OrderLine(Long skuId, Long productId, String productName, String skuSpec, String image,
                  BigDecimal price, Integer quantity) {
            this.skuId = skuId;
            this.productId = productId;
            this.productName = productName;
            this.skuSpec = skuSpec;
            this.image = image;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private static class ProductNameHolder {
        String value;

        ProductNameHolder(String value) {
            this.value = value;
        }
    }
}
