package com.shop.notify.sender;

public interface NotificationSender {

    String channel();

    boolean send(String target, String title, String content);
}

