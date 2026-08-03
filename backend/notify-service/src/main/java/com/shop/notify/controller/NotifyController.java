package com.shop.notify.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.notify.entity.Announcement;
import com.shop.notify.entity.NotifyMessage;
import com.shop.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping("/announcements")
    public Result<List<Announcement>> announcements() {
        return Result.ok(notifyService.listAnnouncements());
    }

    @GetMapping("/messages")
    public Result<PageResult<NotifyMessage>> messages(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) Integer readStatus) {
        return Result.ok(notifyService.userMessages(UserContext.getUserId(), current, size, readStatus));
    }

    @GetMapping("/messages/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notifyService.unreadCount(UserContext.getUserId()));
    }

    @PostMapping("/messages/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notifyService.markRead(UserContext.getUserId(), id);
        return Result.ok();
    }
}

