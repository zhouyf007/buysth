package com.shop.logistics.service;

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
import com.shop.logistics.dto.ShipmentVO;
import com.shop.logistics.dto.TrackRequest;
import com.shop.logistics.entity.Shipment;
import com.shop.logistics.entity.ShipmentTrack;
import com.shop.logistics.mapper.ShipmentMapper;
import com.shop.logistics.mapper.ShipmentTrackMapper;
import com.shop.logistics.provider.LogisticsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentTrackMapper trackMapper;
    private final LogisticsProvider provider;
    private final OrderClient orderClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Shipment createShipment(String orderNo, String operator) {
        Shipment exist = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getOrderNo, orderNo));
        if (exist != null) {
            return exist;
        }
        OrderInfoDTO order = getOrder(orderNo);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new BizException("订单未支付，不能创建运单");
        }
        String shipmentNo = OrderNoGenerator.generate("L");
        Shipment shipment = new Shipment();
        shipment.setShipmentNo(shipmentNo);
        shipment.setOrderNo(orderNo);
        shipment.setUserId(order.getUserId());
        shipment.setCompanyCode(provider.companyCode());
        shipment.setCompanyName(provider.companyName());
        shipment.setTrackingNo(provider.createTrackingNo(shipmentNo));
        shipment.setStatus("CREATED");
        shipment.setReceiverName(order.getReceiverName());
        shipment.setReceiverPhone(order.getReceiverPhone());
        shipment.setReceiverAddress(order.getReceiverAddress());
        shipmentMapper.insert(shipment);
        addTrackInternal(shipment.getId(), shipment.getTrackingNo(), "CREATED", "商家已发货，等待快递揽收");
        publishShipped(shipment);
        return shipment;
    }

    public ShipmentVO track(String orderNo, Long userId, boolean admin) {
        Shipment shipment = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getOrderNo, orderNo));
        if (shipment == null) {
            throw new BizException(404, "暂无物流信息");
        }
        if (!admin && !shipment.getUserId().equals(userId)) {
            throw new BizException(403, "无权查看该物流信息");
        }
        return toVO(shipment);
    }

    public PageResult<ShipmentVO> adminPage(long current, long size, String keyword, String status) {
        Page<Shipment> page = shipmentMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Shipment>()
                        .eq(status != null && !status.isBlank(), Shipment::getStatus, status)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(Shipment::getOrderNo, keyword)
                                .or().like(Shipment::getTrackingNo, keyword)
                                .or().like(Shipment::getShipmentNo, keyword))
                        .orderByDesc(Shipment::getCreateTime));
        return PageResult.of(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()),
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Transactional
    public void addTrack(Long shipmentId, TrackRequest request) {
        Shipment shipment = shipmentMapper.selectById(shipmentId);
        if (shipment == null) {
            throw new BizException(404, "运单不存在");
        }
        addTrackInternal(shipmentId, shipment.getTrackingNo(), request.getStatus(), request.getDescription());
        shipment.setStatus(request.getStatus());
        shipmentMapper.updateById(shipment);
    }

    @Transactional
    public void updateStatus(Long shipmentId, String status) {
        Shipment shipment = shipmentMapper.selectById(shipmentId);
        if (shipment == null) {
            throw new BizException(404, "运单不存在");
        }
        shipment.setStatus(status);
        shipmentMapper.updateById(shipment);
    }

    private void addTrackInternal(Long shipmentId, String trackingNo, String status, String description) {
        ShipmentTrack track = new ShipmentTrack();
        track.setShipmentId(shipmentId);
        track.setTrackingNo(trackingNo);
        track.setStatus(status);
        track.setDescription(description);
        track.setTrackTime(LocalDateTime.now());
        trackMapper.insert(track);
    }

    private void publishShipped(Shipment shipment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "ORDER_SHIPPED");
            event.put("orderNo", shipment.getOrderNo());
            event.put("shipmentNo", shipment.getShipmentNo());
            rabbitTemplate.convertAndSend(MQConstants.TOPIC_EXCHANGE, MQConstants.KEY_ORDER_SHIPPED,
                    objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("publish shipped event failed, shipmentNo={}", shipment.getShipmentNo(), e);
        }
    }

    private OrderInfoDTO getOrder(String orderNo) {
        try {
            Result<OrderInfoDTO> result = orderClient.getByOrderNo(orderNo);
            return result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            throw new BizException("订单服务暂时不可用");
        }
    }

    private ShipmentVO toVO(Shipment shipment) {
        ShipmentVO vo = new ShipmentVO();
        vo.setId(shipment.getId());
        vo.setShipmentNo(shipment.getShipmentNo());
        vo.setOrderNo(shipment.getOrderNo());
        vo.setUserId(shipment.getUserId());
        vo.setCompanyCode(shipment.getCompanyCode());
        vo.setCompanyName(shipment.getCompanyName());
        vo.setTrackingNo(shipment.getTrackingNo());
        vo.setStatus(shipment.getStatus());
        vo.setReceiverName(shipment.getReceiverName());
        vo.setReceiverPhone(shipment.getReceiverPhone());
        vo.setReceiverAddress(shipment.getReceiverAddress());
        vo.setCreateTime(shipment.getCreateTime());
        vo.setTracks(trackMapper.selectList(new LambdaQueryWrapper<ShipmentTrack>()
                .eq(ShipmentTrack::getShipmentId, shipment.getId())
                .orderByDesc(ShipmentTrack::getTrackTime)));
        return vo;
    }
}

