package com.shop.common.mq;

public final class MQConstants {

    public static final String TOPIC_EXCHANGE = "shop.event.topic";
    public static final String DIRECT_EXCHANGE = "shop.direct";

    public static final String KEY_ORDER_CREATED = "order.created";
    public static final String KEY_ORDER_PAID = "order.paid";
    public static final String KEY_ORDER_CANCELLED = "order.cancelled";
    public static final String KEY_ORDER_TIMEOUT = "order.timeout";
    public static final String KEY_SECKILL_ORDER_CREATED = "seckill.order.created";
    public static final String KEY_LOGISTICS_CREATED = "logistics.created";
    public static final String KEY_ORDER_SHIPPED = "order.shipped";
    public static final String KEY_NOTIFY_SEND = "notify.send";

    public static final String Q_PRODUCT_ORDER_CREATED = "product.q.order.created";
    public static final String Q_PRODUCT_ORDER_CANCELLED = "product.q.order.cancelled";
    public static final String Q_ORDER_SECKILL = "order.q.seckill.order";
    public static final String Q_ORDER_PAID = "order.q.order.paid";
    public static final String Q_ORDER_TIMEOUT = "order.q.timeout.cancel";
    public static final String Q_ORDER_TIMEOUT_TTL = "order.q.timeout.ttl";
    public static final String Q_ORDER_SHIPPED = "order.q.order.shipped";
    public static final String Q_LOGISTICS_ORDER_PAID = "logistics.q.order.paid";
    public static final String Q_NOTIFY_SEND = "notify.q.notify.send";

    public static final long ORDER_TIMEOUT_MS = 15 * 60 * 1000L;

    private MQConstants() {
    }
}
