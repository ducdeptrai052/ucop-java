-- Tạo DB
CREATE DATABASE IF NOT EXISTS ucop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ucop;

SET FOREIGN_KEY_CHECKS = 0;

-- Xóa bảng cũ (nếu cần)
DROP TABLE IF EXISTS shipments, payments, order_items, orders, cart_items, carts,
    stock_items, warehouses, items, categories,
    account_roles, account_profiles, accounts, roles, promotions;

-- Bảng role
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng account
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    locked TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Profile 1-1
CREATE TABLE account_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(200),
    phone VARCHAR(50),
    address VARCHAR(255),
    account_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_profile_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Quan hệ account-role
CREATE TABLE account_roles (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_ar_acc FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_ar_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Category (đa cấp)
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    parent_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Item
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(18,2) NOT NULL,
    weight DECIMAL(10,3),
    active TINYINT(1) DEFAULT 1,
    category_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_item_cat FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Warehouse
CREATE TABLE warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50),
    name VARCHAR(150),
    location VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- StockItem
CREATE TABLE stock_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT,
    item_id BIGINT,
    onHand INT DEFAULT 0,
    reserved INT DEFAULT 0,
    lowStockThreshold INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_stock_wh FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Promotion
CREATE TABLE promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    discountPercent INT,
    discountValue DECIMAL(18,2),
    startDate DATETIME,
    endDate DATETIME,
    maxUsage INT,
    usedCount INT DEFAULT 0,
    minOrderValue DECIMAL(18,2),
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Cart
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    status VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES accounts(id)
);

-- CartItem
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    item_id BIGINT,
    quantity INT,
    unitPrice DECIMAL(18,2),
    discountAmount DECIMAL(18,2) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_ci_item FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Order
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    status VARCHAR(50),
    orderDate DATETIME,
    subtotal DECIMAL(18,2),
    discountTotal DECIMAL(18,2),
    taxTotal DECIMAL(18,2),
    shippingFee DECIMAL(18,2),
    grandTotal DECIMAL(18,2),
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES accounts(id)
);

-- OrderItem
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    item_id BIGINT,
    quantity INT,
    unitPrice DECIMAL(18,2),
    discountAmount DECIMAL(18,2) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_oi_item FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Payment
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    method VARCHAR(50),
    status VARCHAR(50),
    amount DECIMAL(18,2),
    transactionCode VARCHAR(120),
    paidAt DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Shipment
CREATE TABLE shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    trackingNumber VARCHAR(120),
    carrier VARCHAR(120),
    status VARCHAR(50),
    shippedAt DATETIME,
    deliveredAt DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_ship_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

SET FOREIGN_KEY_CHECKS = 1;

-- Seed dữ liệu
INSERT INTO roles (name) VALUES ('ADMIN'), ('STAFF'), ('CUSTOMER');

-- Mật khẩu SHA-256 (không salt):
-- admin123 -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- staff123 -> 10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6
-- customer123 -> b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6

INSERT INTO accounts (username, password_hash, email, locked) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin@example.com', 0),
('staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'staff@example.com', 0),
('customer', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'cust@example.com', 0);

INSERT INTO account_profiles (account_id, full_name, phone, address) VALUES
(1, 'Admin User', '0900000001', 'Hanoi'),
(2, 'Staff One', '0900000002', 'Hanoi'),
(3, 'Customer One', '0900000003', 'HCM');

INSERT INTO account_roles (account_id, role_id) VALUES
(1, 1), -- admin -> ADMIN
(2, 2), -- staff1 -> STAFF
(3, 3); -- cust1 -> CUSTOMER

-- Category
INSERT INTO categories (name) VALUES ('Electronics'), ('Grocery');

-- Item
INSERT INTO items (sku, name, price, active, category_id) VALUES
('LTP-001', 'Laptop Pro 15', 15000000, 1, 1),
('PHN-001', 'Smartphone X', 8000000, 1, 1),
('APP-001', 'Apple (1kg)', 30000, 1, 2);

-- Warehouse
INSERT INTO warehouses (code, name, location) VALUES
('HN01', 'Kho Hà Nội', 'Hà Nội'),
('HCM01', 'Kho HCM', 'TP.HCM');

-- Stock
INSERT INTO stock_items (warehouse_id, item_id, onHand, reserved, lowStockThreshold) VALUES
(1, 1, 10, 1, 2),
(1, 2, 20, 0, 3),
(2, 3, 100, 0, 10);

-- Promotion (10% giảm, hiệu lực 30 ngày trước đến 30 ngày sau)
INSERT INTO promotions (code, description, discountPercent, discountValue, startDate, endDate, maxUsage, usedCount, minOrderValue)
VALUES ('PROMO10', 'Giảm 10%', 10, NULL, NOW() - INTERVAL 30 DAY, NOW() + INTERVAL 30 DAY, 100, 1, 500000);

-- Đơn hàng mẫu cho cust1
INSERT INTO orders (customer_id, status, orderDate, subtotal, discountTotal, taxTotal, shippingFee, grandTotal)
VALUES (3, 'PAID', NOW() - INTERVAL 1 DAY, 15000000, 0, 1500000, 30000, 16530000);

INSERT INTO order_items (order_id, item_id, quantity, unitPrice, discountAmount)
VALUES (1, 1, 1, 15000000, 0);

INSERT INTO payments (order_id, method, status, amount, transactionCode, paidAt)
VALUES (1, 'GATEWAY', 'PAID', 16530000, 'GW-DEMO-001', NOW() - INTERVAL 1 DAY);

INSERT INTO shipments (order_id, trackingNumber, carrier, status, shippedAt)
VALUES (1, 'GHN123456', 'GHN', 'IN_TRANSIT', NOW() - INTERVAL 12 HOUR);
