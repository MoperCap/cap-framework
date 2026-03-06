-- =====================================================
-- CAP Example 数据库初始化脚本
-- =====================================================
-- 支持的数据库：MySQL、PostgreSQL、H2
-- 创建日期：2026-03-06
-- =====================================================

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    avatar_url VARCHAR(500) COMMENT '头像 URL',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 商品表 (products)
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    sku VARCHAR(100) NOT NULL UNIQUE COMMENT '商品 SKU',
    price DECIMAL(10, 2) NOT NULL COMMENT '销售价格',
    cost_price DECIMAL(10, 2) COMMENT '成本价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    reserved_stock INT NOT NULL DEFAULT 0 COMMENT '已预留库存',
    category VARCHAR(100) COMMENT '商品分类',
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED') DEFAULT 'ACTIVE' COMMENT '商品状态',
    images JSON COMMENT '商品图片列表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    INDEX idx_sku (sku),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =====================================================
-- 3. 订单表 (orders)
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(100) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    payment_amount DECIMAL(12, 2) COMMENT '实际支付金额',
    discount_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '优惠金额',
    status ENUM('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED') DEFAULT 'PENDING' COMMENT '订单状态',
    payment_method VARCHAR(50) COMMENT '支付方式',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    shipped_at TIMESTAMP NULL COMMENT '发货时间',
    delivered_at TIMESTAMP NULL COMMENT '送达时间',
    cancelled_at TIMESTAMP NULL COMMENT '取消时间',
    remark VARCHAR(500) COMMENT '订单备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 4. 订单项目表 (order_items)
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    sku VARCHAR(100) NOT NULL COMMENT '商品 SKU',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称（快照）',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(12, 2) NOT NULL COMMENT '小计',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项目表';

-- =====================================================
-- 5. 库存日志表 (inventory_logs)
-- =====================================================
CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    order_id BIGINT COMMENT '关联订单 ID',
    change_quantity INT NOT NULL COMMENT '变化数量（可以是负数）',
    operation_type ENUM('INCREASE', 'DECREASE', 'ADJUST', 'RESERVE') DEFAULT 'ADJUST' COMMENT '操作类型',
    reason VARCHAR(255) COMMENT '原因',
    before_quantity INT NOT NULL COMMENT '变化前数量',
    after_quantity INT NOT NULL COMMENT '变化后数量',
    operator_id BIGINT COMMENT '操作员 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_product_id (product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存日志表';

-- =====================================================
-- 6. 支付记录表 (payments)
-- =====================================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    payment_method VARCHAR(50) NOT NULL COMMENT '支付方式',
    amount DECIMAL(12, 2) NOT NULL COMMENT '支付金额',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '支付状态',
    transaction_no VARCHAR(100) COMMENT '交易号',
    error_message VARCHAR(500) COMMENT '错误信息',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- =====================================================
-- 7. 购物车表 (shopping_carts)
-- =====================================================
CREATE TABLE IF NOT EXISTS shopping_carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- =====================================================
-- 初始数据插入
-- =====================================================

-- 插入示例用户
INSERT INTO users (username, email, phone, password_hash, avatar_url, status) VALUES
('alice', 'alice@example.com', '13800138000', 'hashed_password_1', 'https://example.com/avatar/alice.jpg', 'ACTIVE'),
('bob', 'bob@example.com', '13800138001', 'hashed_password_2', 'https://example.com/avatar/bob.jpg', 'ACTIVE'),
('charlie', 'charlie@example.com', '13800138002', 'hashed_password_3', 'https://example.com/avatar/charlie.jpg', 'ACTIVE'),
('diana', 'diana@example.com', '13800138003', 'hashed_password_4', 'https://example.com/avatar/diana.jpg', 'ACTIVE'),
('evan', 'evan@example.com', '13800138004', 'hashed_password_5', 'https://example.com/avatar/evan.jpg', 'INACTIVE');

-- 插入示例商品
INSERT INTO products (name, description, sku, price, cost_price, stock, category, status) VALUES
('MacBook Pro 14', '2023 年苹果 MacBook Pro 14 英寸笔记本电脑', 'SKU-001-MBP14', 12999.00, 8000.00, 50, 'Electronics', 'ACTIVE'),
('Dell XPS 13', '戴尔超轻薄笔记本电脑', 'SKU-002-XPSA13', 7999.00, 5000.00, 100, 'Electronics', 'ACTIVE'),
('Logitech MX Master 3', '罗技专业鼠标', 'SKU-003-MXM3', 399.00, 150.00, 500, 'Accessories', 'ACTIVE'),
('Apple Magic Keyboard', '苹果妙控键盘', 'SKU-004-AMK', 699.00, 300.00, 200, 'Accessories', 'ACTIVE'),
('Samsung 4K Monitor', '三星 4K 显示器 32 英寸', 'SKU-005-S4K32', 3999.00, 2500.00, 30, 'Electronics', 'ACTIVE'),
('USB-C Hub 7in1', '多功能 USB-C 集线器', 'SKU-006-HUB', 299.00, 100.00, 300, 'Accessories', 'ACTIVE'),
('Mechanical Keyboard RGB', '机械键盘 RGB 背光', 'SKU-007-MKRGB', 599.00, 250.00, 150, 'Accessories', 'ACTIVE'),
('Wireless Charger', '无线充电器', 'SKU-008-WCHG', 199.00, 50.00, 400, 'Accessories', 'ACTIVE'),
('4K Webcam', '4K 高清摄像头', 'SKU-009-4KWC', 899.00, 400.00, 80, 'Accessories', 'ACTIVE'),
('Portable SSD 1TB', '便携式固态硬盘 1TB', 'SKU-010-SSD1T', 699.00, 350.00, 120, 'Storage', 'ACTIVE');

-- 插入示例订单
INSERT INTO orders (order_no, user_id, total_amount, payment_amount, discount_amount, status, payment_method, paid_at) VALUES
('ORD-2026030601', 1, 13398.00, 13398.00, 0.00, 'PAID', 'CREDIT_CARD', '2026-03-06 10:30:00'),
('ORD-2026030602', 2, 1298.00, 1298.00, 0.00, 'PAID', 'ALIPAY', '2026-03-06 10:45:00'),
('ORD-2026030603', 3, 2097.00, 2097.00, 100.00, 'PAID', 'WECHAT', '2026-03-06 11:00:00'),
('ORD-2026030604', 1, 4497.00, 4497.00, 0.00, 'PENDING', NULL, NULL),
('ORD-2026030605', 2, 899.00, 899.00, 0.00, 'PAID', 'CREDIT_CARD', '2026-03-06 11:30:00');

-- 插入订单项目
INSERT INTO order_items (order_id, product_id, sku, product_name, unit_price, quantity, subtotal) VALUES
(1, 1, 'SKU-001-MBP14', 'MacBook Pro 14', 12999.00, 1, 12999.00),
(1, 3, 'SKU-003-MXM3', 'Logitech MX Master 3', 399.00, 1, 399.00),
(2, 2, 'SKU-002-XPSA13', 'Dell XPS 13', 7999.00, 1, 7999.00),
(2, 4, 'SKU-004-AMK', 'Apple Magic Keyboard', 699.00, 1, 699.00),
(3, 5, 'SKU-005-S4K32', 'Samsung 4K Monitor', 3999.00, 1, 3999.00),
(3, 6, 'SKU-006-HUB', 'USB-C Hub 7in1', 299.00, 2, 598.00),
(4, 7, 'SKU-007-MKRGB', 'Mechanical Keyboard RGB', 599.00, 1, 599.00),
(4, 8, 'SKU-008-WCHG', 'Wireless Charger', 199.00, 2, 398.00),
(4, 9, 'SKU-009-4KWC', '4K Webcam', 899.00, 1, 899.00),
(4, 10, 'SKU-010-SSD1T', 'Portable SSD 1TB', 699.00, 1, 699.00),
(5, 3, 'SKU-003-MXM3', 'Logitech MX Master 3', 399.00, 2, 798.00),
(5, 8, 'SKU-008-WCHG', 'Wireless Charger', 199.00, 1, 199.00);

-- 插入支付记录
INSERT INTO payments (order_id, user_id, payment_method, amount, status, transaction_no, paid_at) VALUES
(1, 1, 'CREDIT_CARD', 13398.00, 'SUCCESS', 'TXN-20260306-001', '2026-03-06 10:30:00'),
(2, 2, 'ALIPAY', 1298.00, 'SUCCESS', 'TXN-20260306-002', '2026-03-06 10:45:00'),
(3, 3, 'WECHAT', 2097.00, 'SUCCESS', 'TXN-20260306-003', '2026-03-06 11:00:00'),
(5, 2, 'CREDIT_CARD', 899.00, 'SUCCESS', 'TXN-20260306-004', '2026-03-06 11:30:00');

-- 插入购物车数据
INSERT INTO shopping_carts (user_id, product_id, quantity) VALUES
(1, 2, 1),
(1, 6, 2),
(2, 5, 1),
(3, 7, 1),
(3, 9, 2),
(4, 1, 1),
(4, 3, 3),
(5, 4, 1);

-- 插入库存日志
INSERT INTO inventory_logs (product_id, order_id, change_quantity, operation_type, reason, before_quantity, after_quantity) VALUES
(1, 1, -1, 'DECREASE', '订单 ORD-2026030601 出货', 51, 50),
(3, 1, -1, 'DECREASE', '订单 ORD-2026030601 出货', 501, 500),
(2, 2, -1, 'DECREASE', '订单 ORD-2026030602 出货', 101, 100),
(4, 2, -1, 'DECREASE', '订单 ORD-2026030602 出货', 201, 200),
(5, 3, -1, 'DECREASE', '订单 ORD-2026030603 出货', 31, 30),
(6, 3, -2, 'DECREASE', '订单 ORD-2026030603 出货', 302, 300),
(3, 5, -2, 'DECREASE', '订单 ORD-2026030605 出货', 500, 498),
(8, 5, -1, 'DECREASE', '订单 ORD-2026030605 出货', 401, 400);

-- =====================================================
-- 查看表结构和数据统计
-- =====================================================
-- 显示所有表的统计信息
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME;