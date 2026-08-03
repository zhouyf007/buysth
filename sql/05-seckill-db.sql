USE seckill_db;

DROP TABLE IF EXISTS seckill_record;
DROP TABLE IF EXISTS seckill_product;
DROP TABLE IF EXISTS seckill_activity;

CREATE TABLE seckill_activity (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'SECKILL',
    promotion_code VARCHAR(50),
    discount_type VARCHAR(20),
    discount_value DECIMAL(10,2),
    description VARCHAR(500),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status_time(status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seckill_product (
    id BIGINT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    image VARCHAR(255),
    seckill_price DECIMAL(10,2) NOT NULL,
    seckill_stock INT NOT NULL,
    limit_per_user INT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_activity(activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seckill_record (
    id BIGINT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    seckill_product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(40),
    quantity INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_activity_user(activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO seckill_activity (id, name, type, promotion_code, discount_type, discount_value, description, start_time, end_time, status) VALUES
    (901, '小米14 限时秒杀', 'SECKILL', NULL, NULL, NULL, '小米14 8GB+256GB 限时秒杀，每人限购1件', '2026-08-01 00:00:00', '2026-12-31 23:59:59', 'ONLINE'),
    (902, '开学季数码狂欢 95折', 'PROMOTION', 'back-to-school', 'PERCENT', 95.00, '活动期间全场指定数码商品95折', '2026-08-01 00:00:00', '2026-12-31 23:59:59', 'ONLINE');

INSERT INTO seckill_product (id, activity_id, sku_id, product_id, product_name, image, seckill_price, seckill_stock, limit_per_user, status) VALUES
    (911, 901, 201, 101, '小米14 8GB+256GB', '/images/products/phone-xiaomi14.svg', 3799.00, 100, 1, 1);
