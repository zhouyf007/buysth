package com.shop.notify.controller;

import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.notify.entity.Announcement;
import com.shop.notify.entity.NotifyMessage;
import com.shop.notify.service.NotifyService;
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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminNotifyController {

    private final NotifyService notifyService;

    @GetMapping("/announcements")
    public Result<PageResult<Announcement>> announcements(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size,
                                                          @RequestParam(required = false) String keyword) {
        return Result.ok(notifyService.adminPage(current, size, keyword));
    }

    @PostMapping("/announcements")
    public Result<Void> create(@RequestBody Announcement announcement) {
        notifyService.saveAnnouncement(announcement);
        return Result.ok();
    }

    @PutMapping("/announcements/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        notifyService.updateAnnouncement(id, announcement);
        return Result.ok();
    }

    @DeleteMapping("/announcements/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notifyService.deleteAnnouncement(id);
        return Result.ok();
    }

    @GetMapping("/messages")
    public Result<PageResult<NotifyMessage>> messages(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) Long userId) {
        return Result.ok(notifyService.adminMessages(current, size, userId));
    }
}

