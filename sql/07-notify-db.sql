USE notify_db;

DROP TABLE IF EXISTS notify_log;
DROP TABLE IF EXISTS notify_message;
DROP TABLE IF EXISTS announcement;

CREATE TABLE announcement (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'NOTICE',
    status TINYINT NOT NULL DEFAULT 1,
    publish_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status_time(status, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notify_message (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(40),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(1000),
    read_status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user(user_id, read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notify_log (
    id BIGINT PRIMARY KEY,
    message_id BIGINT,
    channel VARCHAR(20) NOT NULL,
    target VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    error VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO announcement (id, title, content, type, status, publish_time) VALUES
    (1, '平台正式上线公告', '数码商城已完成微服务架构升级，支持秒杀、优惠活动、订单物流全链路在线查询。', 'NOTICE', 1, '2026-08-01 09:00:00'),
    (2, '开学季数码狂欢活动开启', '开学季活动进行中，指定数码商品95折，小米14限时秒杀同步开启。', 'ACTIVITY', 1, '2026-08-01 09:30:00');
