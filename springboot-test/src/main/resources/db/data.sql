-- Seed data for realistic local dev
-- Users
INSERT INTO users (name, email, phone, status, level_code, is_test, created_at, updated_at) VALUES
('Nguyen Van A', 'nguyenvana@example.com', '0123456789', 'ACTIVE', 'PO', 0, NOW(), NOW()),
('Tran Thi B', 'tranthib@example.com', '0987654321', 'ACTIVE', 'CO', 0, NOW(), NOW()),
('Le Minh C', 'leminhc@example.com', '0901122334', 'INACTIVE', 'Manager', 0, NOW(), NOW()),
('Pham Thu D', 'phamthud@example.com', '0912233445', 'SUSPENDED', 'Player', 0, NOW(), NOW()),
('Account Test', 'account_test@example.com', '0900000000', 'ACTIVE', 'Player', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
name = VALUES(name),
phone = VALUES(phone),
status = VALUES(status),
level_code = VALUES(level_code),
is_test = VALUES(is_test),
updated_at = VALUES(updated_at);

-- App config
INSERT INTO app_config (config_key, config_value) VALUES
('exclude_test_data_enabled', 'true')
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value);

-- Profiles
INSERT INTO user_profiles (user_id, date_of_birth, gender, national_id, job_title, company, bio)
VALUES
(1, '1993-02-12', 'FEMALE', '012345678901', 'Accountant', 'Saigon Logistics', 'Enjoys process improvement and analytics.'),
(2, '1990-10-03', 'MALE', '023456789012', 'Sales Lead', 'VietTrade Co', 'Focused on enterprise partnerships.'),
(3, '1997-06-21', 'MALE', '034567890123', 'Developer', 'CloudNine', 'Backend developer specializing in APIs.'),
(4, '1988-12-05', 'FEMALE', '045678901234', 'HR Manager', 'PeopleFirst', 'Leads hiring and culture initiatives.');

-- Addresses
INSERT INTO addresses (user_id, type, line1, line2, city, state, postal_code, country, is_default)
VALUES
(1, 'HOME', '12 Nguyen Hue', NULL, 'Ho Chi Minh', 'HCM', '700000', 'VN', 1),
(1, 'WORK', '150 Dien Bien Phu', 'Floor 5', 'Ho Chi Minh', 'HCM', '700000', 'VN', 0),
(2, 'HOME', '25 Tran Hung Dao', NULL, 'Ha Noi', 'HN', '100000', 'VN', 1),
(3, 'HOME', '8 Vo Nguyen Giap', NULL, 'Da Nang', 'DN', '550000', 'VN', 1);

-- Roles
INSERT INTO roles (code, name, description)
VALUES
('ADMIN', 'Administrator', 'Full access to all resources.'),
('MANAGER', 'Manager', 'Manage users, orders, and products.'),
('CUSTOMER', 'Customer', 'Standard customer permissions.')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description);

-- Permissions
INSERT INTO permissions (code, name, description)
VALUES
('USER_READ', 'Read users', 'View user data.'),
('USER_WRITE', 'Write users', 'Create and update users.'),
('ORDER_READ', 'Read orders', 'View orders.'),
('ORDER_WRITE', 'Write orders', 'Create and update orders.'),
('PRODUCT_READ', 'Read products', 'View product catalog.'),
('PRODUCT_WRITE', 'Write products', 'Manage products and inventory.')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description);

-- Role-permission mapping
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(2, 1), (2, 3), (2, 4), (2, 5), (2, 6),
(3, 3), (3, 5);

-- User-role mapping
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3);

-- Products
INSERT INTO products (sku, name, description, price, currency, status)
VALUES
('SKU-IPH15-128', 'iPhone 15 128GB', 'Latest model smartphone', 22990000, 'VND', 'ACTIVE'),
('SKU-MBP14-512', 'MacBook Pro 14 512GB', 'Laptop for professionals', 55990000, 'VND', 'ACTIVE'),
('SKU-AIRP2', 'AirPods Pro 2', 'Noise cancelling earbuds', 6390000, 'VND', 'ACTIVE'),
('SKU-MOUSE-LOGI', 'Logitech MX Master 3', 'Ergonomic wireless mouse', 2490000, 'VND', 'ACTIVE')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description),
price = VALUES(price),
currency = VALUES(currency),
status = VALUES(status);

-- Inventory
INSERT INTO inventory (product_id, quantity_on_hand, reserved, reorder_level)
VALUES
(1, 40, 2, 10),
(2, 15, 1, 5),
(3, 80, 0, 20),
(4, 120, 3, 25);

-- Orders
INSERT INTO orders (order_number, user_id, status, total_amount, currency)
VALUES
('ORD-20260207-0001', 1, 'PAID', 29380000, 'VND'),
('ORD-20260207-0002', 2, 'PENDING', 6390000, 'VND')
ON DUPLICATE KEY UPDATE
status = VALUES(status),
total_amount = VALUES(total_amount),
currency = VALUES(currency);

-- Order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total)
VALUES
(1, 1, 1, 22990000, 22990000),
(1, 4, 1, 2490000, 2490000),
(1, 3, 1, 6390000, 6390000),
(2, 3, 1, 6390000, 6390000);

-- Audit logs
INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id, before_data, after_data, ip_address)
VALUES
(1, 'CREATE', 'USER', 1, NULL, JSON_OBJECT('email', 'nguyenvana@example.com'), '127.0.0.1'),
(1, 'CREATE', 'ORDER', 1, NULL, JSON_OBJECT('order_number', 'ORD-20260207-0001'), '127.0.0.1');
