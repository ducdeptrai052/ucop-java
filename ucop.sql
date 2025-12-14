-- UCOP MySQL schema & seed (ASCII only)
CREATE DATABASE IF NOT EXISTS ucop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ucop;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS shipments, payments, order_items, orders, cart_items, carts,
    stock_items, warehouses, items, categories,
    account_roles, account_profiles, accounts, roles, promotions;

/* ========== MASTER DATA ========== */
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    locked TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE account_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fullName VARCHAR(200),
    phone VARCHAR(50),
    address VARCHAR(255),
    account_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_profile_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE account_roles (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_ar_acc FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_ar_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

/* ========== CATALOG ========== */
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

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(18,2) NOT NULL,
    weight DECIMAL(10,3),
    active TINYINT(1) DEFAULT 1,
    category_id BIGINT,
    image_url VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_item_cat FOREIGN KEY (category_id) REFERENCES categories(id)
);

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

/* ========== CART & ORDER ========== */
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

/* ========== SEED DATA ========== */
INSERT INTO roles (name) VALUES ('ADMIN'), ('STAFF'), ('CUSTOMER');

-- Password SHA-256 hashes:
-- admin123    240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- staff123    10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6
-- customer123 b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6
INSERT INTO accounts (username, passwordHash, email, locked) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin@example.com', 0),
('staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'staff@example.com', 0),
('customer', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'cust@example.com', 0);

INSERT INTO account_profiles (account_id, fullName, phone, address) VALUES
((SELECT id FROM accounts WHERE username='admin'), 'Admin', '0900000001', 'Ha Noi'),
((SELECT id FROM accounts WHERE username='staff'), 'Staff One', '0900000002', 'Ha Noi'),
((SELECT id FROM accounts WHERE username='customer'), 'Customer One', '0900000003', 'HCM');

INSERT INTO account_roles (account_id, role_id) VALUES
((SELECT id FROM accounts WHERE username='admin'), (SELECT id FROM roles WHERE name='ADMIN')),
((SELECT id FROM accounts WHERE username='staff'), (SELECT id FROM roles WHERE name='STAFF')),
((SELECT id FROM accounts WHERE username='customer'), (SELECT id FROM roles WHERE name='CUSTOMER'));

-- Base categories
INSERT INTO categories (name) VALUES ('Electronics'), ('Grocery');
-- Laptop category fixed id
INSERT INTO categories (id, name, created_at) VALUES (1001, 'Laptop', NOW());

-- Warehouses
INSERT INTO warehouses (code, name, location) VALUES
('HN01', 'Kho Ha Noi', 'Ha Noi'),
('HCM01', 'Kho HCM', 'TP.HCM');

-- MacBook items
INSERT INTO items (sku, name, description, price, weight, active, category_id, image_url, created_at) VALUES
('MBP-14-M1', 'MacBook Pro 14 M1', 'M1 Pro 16GB/512GB', 38000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-14-M1MAX', 'MacBook Pro 14 M1 Max', 'M1 Max 32GB/1TB', 52000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-16-M1', 'MacBook Pro 16 M1 Pro', 'M1 Pro 16GB/512GB', 43000000.00, 2.100, 1, 1001, NULL, NOW()),
('MBP-16-M1MAX', 'MacBook Pro 16 M1 Max', 'M1 Max 32GB/1TB', 57000000.00, 2.100, 1, 1001, NULL, NOW()),
('MBP-13-M1', 'MacBook Pro 13 M1', 'M1 8GB/256GB', 28000000.00, 1.400, 1, 1001, NULL, NOW()),
('MBA-13-M1', 'MacBook Air 13 M1', 'M1 8GB/256GB', 20000000.00, 1.290, 1, 1001, NULL, NOW()),
('MBA-13-M2', 'MacBook Air 13 M2', 'M2 8GB/256GB', 24000000.00, 1.240, 1, 1001, NULL, NOW()),
('MBA-15-M2', 'MacBook Air 15 M2', 'M2 8GB/512GB', 29000000.00, 1.510, 1, 1001, NULL, NOW()),
('MBP-14-M2', 'MacBook Pro 14 M2 Pro', 'M2 Pro 16GB/512GB', 41000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-16-M2', 'MacBook Pro 16 M2 Pro', 'M2 Pro 16GB/512GB', 46000000.00, 2.140, 1, 1001, NULL, NOW()),
('MBP-14-M2MAX', 'MacBook Pro 14 M2 Max', 'M2 Max 32GB/1TB', 56000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-16-M2MAX', 'MacBook Pro 16 M2 Max', 'M2 Max 32GB/1TB', 61000000.00, 2.140, 1, 1001, NULL, NOW()),
('MBP-13-M2', 'MacBook Pro 13 M2', 'M2 8GB/256GB', 31000000.00, 1.400, 1, 1001, NULL, NOW()),
('MBA-13-M3', 'MacBook Air 13 M3', 'M3 8GB/256GB', 26000000.00, 1.240, 1, 1001, NULL, NOW()),
('MBA-15-M3', 'MacBook Air 15 M3', 'M3 8GB/512GB', 32000000.00, 1.510, 1, 1001, NULL, NOW()),
('MBP-14-M3', 'MacBook Pro 14 M3 Pro', 'M3 Pro 16GB/512GB', 44000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-16-M3', 'MacBook Pro 16 M3 Pro', 'M3 Pro 16GB/512GB', 49000000.00, 2.140, 1, 1001, NULL, NOW()),
('MBP-14-M3MAX', 'MacBook Pro 14 M3 Max', 'M3 Max 32GB/1TB', 64000000.00, 1.600, 1, 1001, NULL, NOW()),
('MBP-16-M3MAX', 'MacBook Pro 16 M3 Max', 'M3 Max 48GB/1TB', 70000000.00, 2.140, 1, 1001, NULL, NOW()),
('MBA-13-REF', 'MacBook Air 13 Refurb', 'Air 13 refurbished', 17000000.00, 1.290, 1, 1001, NULL, NOW());

-- Stock after items exist
INSERT INTO stock_items (warehouse_id, item_id, onHand, reserved, lowStockThreshold)
VALUES
((SELECT id FROM warehouses WHERE code='HN01'),
 (SELECT id FROM items WHERE sku='MBP-14-M1'), 10, 1, 2),
((SELECT id FROM warehouses WHERE code='HN01'),
 (SELECT id FROM items WHERE sku='MBP-13-M1'), 20, 0, 3),
((SELECT id FROM warehouses WHERE code='HCM01'),
 (SELECT id FROM items WHERE sku='MBA-13-M1'), 100, 0, 10);

-- Promotion sample
INSERT INTO promotions (code, description, discountPercent, discountValue, startDate, endDate, maxUsage, usedCount, minOrderValue)
VALUES ('PROMO10', 'Giam 10%', 10, NULL, NOW() - INTERVAL 30 DAY, NOW() + INTERVAL 30 DAY, 100, 1, 500000);

-- Sample order for customer
INSERT INTO orders (customer_id, status, orderDate, subtotal, discountTotal, taxTotal, shippingFee, grandTotal)
VALUES ((SELECT id FROM accounts WHERE username='customer'), 'PAID', NOW() - INTERVAL 1 DAY, 15000000, 0, 1500000, 30000, 16530000);

INSERT INTO order_items (order_id, item_id, quantity, unitPrice, discountAmount)
VALUES (
    (SELECT id FROM orders ORDER BY id DESC LIMIT 1),
    (SELECT id FROM items WHERE sku='MBP-14-M1' LIMIT 1),
    1, 15000000, 0);

INSERT INTO payments (order_id, method, status, amount, transactionCode, paidAt)
VALUES (
    (SELECT id FROM orders ORDER BY id DESC LIMIT 1),
    'GATEWAY', 'PAID', 16530000, 'GW-DEMO-001', NOW() - INTERVAL 1 DAY);

INSERT INTO shipments (order_id, trackingNumber, carrier, status, shippedAt)
VALUES (
    (SELECT id FROM orders ORDER BY id DESC LIMIT 1),
    'GHN123456', 'GHN', 'IN_TRANSIT', NOW() - INTERVAL 12 HOUR);
