package com.shop.notify.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "shop.notify.sender", havingValue = "console", matchIfMissing = true)
public class ConsoleNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "console";
    }

    @Override
    public boolean send(String target, String title, String content) {
        log.info("[NOTIFY] target={}, title={}, content={}", target, title, content);
        return true;
    }
}

