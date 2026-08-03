USE product_db;

DROP TABLE IF EXISTS product_sku;
DROP TABLE IF EXISTS product_review;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;

CREATE TABLE category (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(255),
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    subtitle VARCHAR(255),
    brand VARCHAR(50),
    region VARCHAR(50),
    main_image VARCHAR(255),
    detail TEXT,
    publish_date DATETIME,
    status TINYINT NOT NULL DEFAULT 1,
    sales INT NOT NULL DEFAULT 0,
    rating DECIMAL(3,1) NOT NULL DEFAULT 5.0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_category(category_id),
    INDEX idx_status_publish(status, publish_date),
    INDEX idx_region(region),
    FULLTEXT INDEX ft_name(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    spec_name VARCHAR(50) NOT NULL,
    spec_value VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_product(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_review (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(50),
    rating TINYINT NOT NULL,
    content VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_product(product_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO category (id, parent_id, name, icon, sort) VALUES
    (1, 0, '手机', '/images/categories/phone.svg', 1),
    (2, 0, '笔记本', '/images/categories/laptop.svg', 2),
    (3, 0, '平板电脑', '/images/categories/tablet.svg', 3),
    (4, 0, '耳机', '/images/categories/earphone.svg', 4),
    (5, 0, '智能手表', '/images/categories/watch.svg', 5),
    (6, 0, '数码配件', '/images/categories/accessory.svg', 6);

INSERT INTO product (id, category_id, name, subtitle, brand, region, main_image, detail, publish_date, status, sales, rating) VALUES
    (101, 1, '小米14 骁龙8Gen3 5G手机', '徕卡光学镜头，小米澎湃OS', '小米', '深圳', '/images/products/phone-xiaomi14.svg', '<p>第三代骁龙8移动平台，徕卡光学Summilux镜头，5000mAh大电池。</p>', '2026-07-18 10:00:00', 1, 3280, 4.8),
    (102, 1, '华为Mate 60 Pro 卫星通话手机', '昆仑玻璃，卫星通话，鸿蒙系统', '华为', '深圳', '/images/products/phone-huawei60.svg', '<p>超可靠玄武架构，天通卫星通话，第二代昆仑玻璃。</p>', '2026-07-12 10:00:00', 1, 4120, 4.9),
    (103, 1, 'iPhone 15 Pro 钛金属手机', 'A17 Pro芯片，钛金属设计', '苹果', '上海', '/images/products/phone-iphone15.svg', '<p>A17 Pro芯片，USB-C接口，钛金属机身，4800万像素主摄。</p>', '2026-06-30 09:00:00', 1, 5230, 4.7),
    (104, 2, '联想拯救者Y9000P 电竞本', '24核酷睿i9，RTX4070', '联想', '北京', '/images/products/laptop-lenovo.svg', '<p>16英寸2.5K电竞屏，240Hz高刷，霜刃Pro散热系统。</p>', '2026-07-20 10:00:00', 1, 1860, 4.8),
    (105, 2, 'MacBook Air M2 轻薄本', 'M2芯片，18小时续航', '苹果', '上海', '/images/products/laptop-macbook.svg', '<p>M2芯片，Liquid视网膜显示屏，无风扇静音设计。</p>', '2026-07-05 10:00:00', 1, 2940, 4.9),
    (106, 3, '小米平板6 Pro 11英寸', '2.8K 144Hz高刷屏', '小米', '南京', '/images/products/tablet-xiaomi6.svg', '<p>骁龙8+处理器，2.8K超清屏，144Hz刷新率。</p>', '2026-07-15 10:00:00', 1, 1120, 4.6),
    (107, 4, '漫步者NeoBuds Pro 2 降噪耳机', '-50dB深度降噪，Hi-Res音质', '漫步者', '深圳', '/images/products/earphone-edifier.svg', '<p>蓝牙5.3，深度降噪，空间音频，支持LDAC。</p>', '2026-07-22 10:00:00', 1, 3680, 4.7),
    (108, 4, '索尼WH-1000XM5 头戴式降噪耳机', '行业标杆降噪，30小时续航', '索尼', '上海', '/images/products/earphone-sony.svg', '<p>新一代集成处理器V1，8麦克风降噪，佩戴舒适。</p>', '2026-06-28 10:00:00', 1, 2090, 4.8),
    (109, 5, 'Apple Watch Series 9 智能手表', 'S9芯片，全天候视网膜屏', '苹果', '上海', '/images/products/watch-apple9.svg', '<p>健康监测，双指互点手势，Siri智能交互。</p>', '2026-07-08 10:00:00', 1, 1760, 4.6),
    (110, 5, '小米手环8 Pro 大屏手环', '1.74英寸AMOLED大屏', '小米', '南京', '/images/products/watch-xiaomi8.svg', '<p>150+运动模式，血氧心率监测，14天续航。</p>', '2026-07-16 10:00:00', 1, 4520, 4.7),
    (111, 6, '绿联65W氮化镓充电器', '三口快充，小巧便携', '绿联', '深圳', '/images/products/accessory-gan65.svg', '<p>氮化镓技术，65W快充，兼容手机笔记本。</p>', '2026-07-10 10:00:00', 1, 6100, 4.8),
    (112, 6, '罗技MX Master 3S 无线鼠标', '8K DPI，静音按键', '罗技', '苏州', '/images/products/accessory-mouse.svg', '<p>MagSpeed电磁滚轮，多设备切换，人体工学设计。</p>', '2026-07-02 10:00:00', 1, 2380, 4.9);

INSERT INTO product_sku (id, product_id, spec_name, spec_value, price, stock, image, status) VALUES
    (201, 101, '版本', '8GB+256GB', 4299.00, 120, '/images/products/phone-xiaomi14.svg', 1),
    (202, 101, '版本', '12GB+512GB', 4699.00, 80, '/images/products/phone-xiaomi14.svg', 1),
    (203, 102, '版本', '12GB+512GB', 6999.00, 60, '/images/products/phone-huawei60.svg', 1),
    (204, 102, '版本', '12GB+1TB', 7999.00, 40, '/images/products/phone-huawei60.svg', 1),
    (205, 103, '版本', '256GB', 8999.00, 100, '/images/products/phone-iphone15.svg', 1),
    (206, 103, '版本', '512GB', 10999.00, 60, '/images/products/phone-iphone15.svg', 1),
    (207, 104, '版本', 'i9+RTX4070+32GB', 10999.00, 35, '/images/products/laptop-lenovo.svg', 1),
    (208, 105, '版本', '8GB+256GB', 7999.00, 50, '/images/products/laptop-macbook.svg', 1),
    (209, 106, '版本', '8GB+128GB', 2499.00, 90, '/images/products/tablet-xiaomi6.svg', 1),
    (210, 107, '颜色', '曜石黑', 1299.00, 200, '/images/products/earphone-edifier.svg', 1),
    (211, 107, '颜色', '云影灰', 1299.00, 180, '/images/products/earphone-edifier.svg', 1),
    (212, 108, '颜色', '铂金银', 2499.00, 120, '/images/products/earphone-sony.svg', 1),
    (213, 109, '表壳', '41mm星光色', 2999.00, 80, '/images/products/watch-apple9.svg', 1),
    (214, 110, '表带', '黑色硅胶', 399.00, 300, '/images/products/watch-xiaomi8.svg', 1),
    (215, 111, '颜色', '白色', 129.00, 500, '/images/products/accessory-gan65.svg', 1),
    (216, 112, '颜色', '深灰色', 699.00, 260, '/images/products/accessory-mouse.svg', 1);

INSERT INTO product (id, category_id, name, subtitle, brand, region, main_image, detail, publish_date, status, sales, rating) VALUES
    (113, 1, '荣耀Magic6 Pro 5G手机', '青海湖电池，潜望长焦', '荣耀', '深圳', '/images/products/phone-huawei60.svg', '<p>第二代青海湖电池，5600mAh大容量，潜望式长焦支持百倍变焦。</p>', '2026-07-25 10:00:00', 1, 980, 4.6),
    (114, 2, '华为MateBook X Pro 笔记本', '980g超轻，3.1K触控屏', '华为', '深圳', '/images/products/laptop-macbook.svg', '<p>980克超轻机身，3.1K原色触控屏，超级终端互联。</p>', '2026-07-24 10:00:00', 1, 420, 4.8),
    (115, 3, '荣耀平板9 标准版', '12.1英寸120Hz护眼屏', '荣耀', '南京', '/images/products/tablet-xiaomi6.svg', '<p>12.1英寸2.5K护眼屏，120Hz高刷，八扬声器环绕音效。</p>', '2026-07-23 10:00:00', 1, 1560, 4.5),
    (116, 4, '小米Redmi Buds 5 真无线耳机', '46dB主动降噪', '小米', '南京', '/images/products/earphone-edifier.svg', '<p>46dB深度主动降噪，蓝牙5.3，单次续航10小时。</p>', '2026-07-22 10:00:00', 1, 2680, 4.6),
    (117, 5, '华为WATCH GT 5 智能手表', '两周长续航，北斗定位', '华为', '深圳', '/images/products/watch-apple9.svg', '<p>玄玑感知系统，两周长续航，五星北斗定位。</p>', '2026-07-21 10:00:00', 1, 1320, 4.7),
    (118, 6, '倍思100W氮化镓充电器', '四口快充，桌面充电站', '倍思', '苏州', '/images/products/accessory-gan65.svg', '<p>100W大功率，2C2A四口输出，智能分配功率。</p>', '2026-07-20 10:00:00', 1, 3420, 4.7),
    (119, 1, '三星Galaxy S24 Ultra 手机', '2亿像素，AI影像', '三星', '上海', '/images/products/phone-iphone15.svg', '<p>2亿像素主摄，Galaxy AI，钛金属边框。</p>', '2026-07-19 10:00:00', 1, 760, 4.6),
    (120, 2, '惠普暗影精灵9 电竞本', '13代酷睿+RTX4060', '惠普', '北京', '/images/products/laptop-lenovo.svg', '<p>16.1英寸2.5K电竞屏，狂暴模式性能释放。</p>', '2026-07-18 10:00:00', 1, 890, 4.5);

INSERT INTO product_sku (id, product_id, spec_name, spec_value, price, stock, image, status) VALUES
    (217, 113, '版本', '12GB+256GB', 5699.00, 70, '/images/products/phone-huawei60.svg', 1),
    (218, 114, '版本', '16GB+1TB', 11999.00, 30, '/images/products/laptop-macbook.svg', 1),
    (219, 115, '版本', '8GB+128GB', 1499.00, 150, '/images/products/tablet-xiaomi6.svg', 1),
    (220, 116, '颜色', '白色', 299.00, 500, '/images/products/earphone-edifier.svg', 1),
    (221, 117, '表壳', '46mm曜金黑', 1488.00, 90, '/images/products/watch-apple9.svg', 1),
    (222, 118, '颜色', '黑色', 199.00, 400, '/images/products/accessory-gan65.svg', 1),
    (223, 119, '版本', '12GB+256GB', 9699.00, 45, '/images/products/phone-iphone15.svg', 1),
    (224, 120, '版本', 'i9+RTX4060+16GB', 8999.00, 40, '/images/products/laptop-lenovo.svg', 1);
