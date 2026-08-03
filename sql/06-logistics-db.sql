USE logistics_db;

DROP TABLE IF EXISTS shipment_track;
DROP TABLE IF EXISTS shipment;

CREATE TABLE shipment (
    id BIGINT PRIMARY KEY,
    shipment_no VARCHAR(40) NOT NULL UNIQUE,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    company_code VARCHAR(30),
    company_name VARCHAR(50),
    tracking_no VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    admin_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_order_no(order_no),
    INDEX idx_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shipment_track (
    id BIGINT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    tracking_no VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    track_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_shipment(shipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
