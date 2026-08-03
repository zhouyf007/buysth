USE auth_db;

DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_menu;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_username(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_role(user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    icon VARCHAR(50),
    permission_code VARCHAR(100),
    type VARCHAR(20) NOT NULL DEFAULT 'MENU',
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_role_menu(role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_role (id, code, name, description) VALUES
    (1, 'SUPER_ADMIN', '超级管理员', '拥有全部权限'),
    (2, 'OPERATOR', '运营人员', '商品、活动、公告运营权限'),
    (3, 'USER', '普通用户', '用户端默认角色');

INSERT INTO sys_menu (id, parent_id, name, path, icon, permission_code, type, sort) VALUES
    (1, 0, '工作台', '/dashboard', 'Odometer', 'dashboard:view', 'MENU', 1),
    (10, 0, '商品管理', '', 'Goods', 'product:view', 'MENU', 10),
    (11, 10, '分类管理', '/category', '', 'category:list', 'MENU', 11),
    (12, 10, '商品列表', '/product', '', 'product:list', 'MENU', 12),
    (20, 0, '订单管理', '/order', 'Tickets', 'order:list', 'MENU', 20),
    (30, 0, '支付管理', '/payment', 'Wallet', 'pay:list', 'MENU', 30),
    (40, 0, '营销管理', '', 'Promotion', 'seckill:view', 'MENU', 40),
    (41, 40, '活动管理', '/activity', '', 'activity:list', 'MENU', 41),
    (42, 40, '秒杀商品', '/seckill', '', 'seckill:list', 'MENU', 42),
    (50, 0, '物流管理', '/logistics', 'Van', 'logistics:list', 'MENU', 50),
    (60, 0, '用户管理', '/user', 'User', 'user:list', 'MENU', 60),
    (70, 0, '公告管理', '/announcement', 'Bell', 'announce:list', 'MENU', 70),
    (80, 0, '系统管理', '', 'Setting', 'system:view', 'MENU', 80),
    (81, 80, '角色管理', '/role', '', 'role:list', 'MENU', 81);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
    (1, 1, 1), (2, 1, 10), (3, 1, 11), (4, 1, 12), (5, 1, 20), (6, 1, 30),
    (7, 1, 40), (8, 1, 41), (9, 1, 42), (10, 1, 50), (11, 1, 60), (12, 1, 70),
    (13, 1, 80), (14, 1, 81),
    (15, 2, 10), (16, 2, 11), (17, 2, 12), (18, 2, 40), (19, 2, 41), (20, 2, 42), (21, 2, 70);
