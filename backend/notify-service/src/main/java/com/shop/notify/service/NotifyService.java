package com.shop.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.api.dto.OrderInfoDTO;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.api.dto.UserDTO;
import com.shop.api.feign.AuthClient;
import com.shop.api.feign.OrderClient;
import com.shop.common.exception.BizException;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.notify.entity.Announcement;
import com.shop.notify.entity.NotifyLog;
import com.shop.notify.entity.NotifyMessage;
import com.shop.notify.mapper.AnnouncementMapper;
import com.shop.notify.mapper.NotifyLogMapper;
import com.shop.notify.mapper.NotifyMessageMapper;
import com.shop.notify.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final AnnouncementMapper announcementMapper;
    private final NotifyMessageMapper messageMapper;
    private final NotifyLogMapper logMapper;
    private final NotificationSender sender;
    private final AuthClient authClient;
    private final OrderClient orderClient;

    public void onOrderPaid(Map<String, Object> event) {
        String orderNo = String.valueOf(event.get("orderNo"));
        OrderInfoDTO order = getOrder(orderNo);
        if (order == null) {
            return;
        }
        String amount = event.get("amount") == null ? "" : String.valueOf(event.get("amount"));
        createAndSend(order.getUserId(), "ORDER_PAID", orderNo, "订单支付成功",
                "您的订单 " + orderNo + " 已支付成功，支付金额 ¥" + amount + "，商品正在打包中。");
    }

    public void onOrderCancelled(Map<String, Object> event) {
        String orderNo = String.valueOf(event.get("orderNo"));
        OrderInfoDTO order = getOrder(orderNo);
        if (order == null) {
            return;
        }
        createAndSend(order.getUserId(), "ORDER_CANCELLED", orderNo, "订单已取消",
                "您的订单 " + orderNo + " 已取消，如有疑问请联系客服。");
    }

    public void onOrderShipped(Map<String, Object> event) {
        String orderNo = String.valueOf(event.get("orderNo"));
        String shipmentNo = String.valueOf(event.get("shipmentNo"));
        OrderInfoDTO order = getOrder(orderNo);
        if (order == null) {
            return;
        }
        createAndSend(order.getUserId(), "ORDER_SHIPPED", orderNo, "订单已发货",
                "您的订单 " + orderNo + " 已发货，运单号 " + shipmentNo + "，可前往订单详情查看物流。");
    }

    public void onSeckillSuccess(SeckillOrderRequest request) {
        createAndSend(request.getUserId(), "SECKILL_SUCCESS", request.getOrderNo(), "秒杀抢购成功",
                "您已成功抢购 " + request.getProductName() + "，请尽快在订单中补充收货信息并完成支付。");
    }

    public List<Announcement> listAnnouncements() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .le(Announcement::getPublishTime, LocalDateTime.now())
                .orderByDesc(Announcement::getPublishTime));
    }

    public PageResult<Announcement> adminPage(long current, long size, String keyword) {
        Page<Announcement> page = announcementMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Announcement>()
                        .and(StringUtils.hasText(keyword), w -> w.like(Announcement::getTitle, keyword)
                                .or().like(Announcement::getContent, keyword))
                        .orderByDesc(Announcement::getCreateTime));
        return PageResult.of(page);
    }

    public void saveAnnouncement(Announcement announcement) {
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        announcementMapper.insert(announcement);
    }

    public void updateAnnouncement(Long id, Announcement announcement) {
        if (announcementMapper.selectById(id) == null) {
            throw new BizException(404, "公告不存在");
        }
        announcement.setId(id);
        announcementMapper.updateById(announcement);
    }

    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    public PageResult<NotifyMessage> userMessages(Long userId, long current, long size, Integer readStatus) {
        Page<NotifyMessage> page = messageMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<NotifyMessage>()
                        .eq(NotifyMessage::getUserId, userId)
                        .eq(NotifyMessage::getUserDeleted, 0)
                        .eq(readStatus != null, NotifyMessage::getReadStatus, readStatus)
                        .orderByDesc(NotifyMessage::getCreateTime));
        return PageResult.of(page);
    }

    public long unreadCount(Long userId) {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<NotifyMessage>()
                .eq(NotifyMessage::getUserId, userId)
                .eq(NotifyMessage::getUserDeleted, 0)
                .eq(NotifyMessage::getReadStatus, 0));
        return count == null ? 0 : count;
    }

    public void markRead(Long userId, Long messageId) {
        NotifyMessage message = messageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(404, "消息不存在");
        }
        message.setReadStatus(1);
        messageMapper.updateById(message);
    }

    public void deleteUserMessage(Long userId, Long messageId) {
        NotifyMessage message = messageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(404, "消息不存在");
        }
        message.setUserDeleted(1);
        messageMapper.updateById(message);
    }

    public void batchDeleteUserMessages(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        messageMapper.update(null, new LambdaUpdateWrapper<NotifyMessage>()
                .eq(NotifyMessage::getUserId, userId)
                .in(NotifyMessage::getId, ids)
                .set(NotifyMessage::getUserDeleted, 1));
    }

    public void adminDeleteMessage(Long messageId) {
        NotifyMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(404, "消息不存在");
        }
        message.setAdminDeleted(1);
        messageMapper.updateById(message);
    }

    public void adminBatchDeleteMessages(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        messageMapper.update(null, new LambdaUpdateWrapper<NotifyMessage>()
                .in(NotifyMessage::getId, ids)
                .set(NotifyMessage::getAdminDeleted, 1));
    }

    public PageResult<NotifyMessage> adminMessages(long current, long size, Long userId) {
        Page<NotifyMessage> page = messageMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<NotifyMessage>()
                        .eq(userId != null, NotifyMessage::getUserId, userId)
                        .eq(NotifyMessage::getAdminDeleted, 0)
                        .orderByDesc(NotifyMessage::getCreateTime));
        return PageResult.of(page);
    }

    private void createAndSend(Long userId, String type, String orderNo, String title, String content) {
        NotifyMessage message = new NotifyMessage();
        message.setUserId(userId);
        message.setOrderNo(orderNo);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setReadStatus(0);
        messageMapper.insert(message);

        UserDTO user = getUser(userId);
        if (user == null) {
            return;
        }
        if (StringUtils.hasText(user.getEmail())) {
            send(message.getId(), "email:" + user.getEmail(), title, content);
        }
        if (StringUtils.hasText(user.getPhone())) {
            send(message.getId(), "sms:" + user.getPhone(), title, content);
        }
    }

    private void send(Long messageId, String target, String title, String content) {
        NotifyLog log = new NotifyLog();
        log.setMessageId(messageId);
        log.setChannel(sender.channel());
        log.setTarget(target);
        try {
            boolean ok = sender.send(target, title, content);
            log.setStatus(ok ? "SUCCESS" : "FAILED");
            if (!ok) {
                log.setError("发送失败");
            }
        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setError(e.getMessage());
        }
        logMapper.insert(log);
    }

    private OrderInfoDTO getOrder(String orderNo) {
        try {
            Result<OrderInfoDTO> result = orderClient.getByOrderNo(orderNo);
            return result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            log.warn("get order failed, orderNo={}", orderNo, e);
            return null;
        }
    }

    private UserDTO getUser(Long userId) {
        try {
            Result<UserDTO> result = authClient.getUser(userId);
            return result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            log.warn("get user failed, userId={}", userId, e);
            return null;
        }
    }
}
