-- Test seed data
SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE inventory;
TRUNCATE TABLE products;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE permissions;
TRUNCATE TABLE roles;
TRUNCATE TABLE addresses;
TRUNCATE TABLE user_profiles;
TRUNCATE TABLE users;
TRUNCATE TABLE app_config;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO users (name, email, phone, status, level_code, is_test, created_at, updated_at) VALUES
('Test User One', 'test1@example.com', '0900000001', 'ACTIVE', 'PO', 0, NOW(), NOW()),
('Test User Two', 'test2@example.com', '0900000002', 'ACTIVE', 'CO', 0, NOW(), NOW()),
('Account Test', 'account_test@example.com', '0900000000', 'ACTIVE', 'Player', 1, NOW(), NOW());

INSERT INTO app_config (config_key, config_value) VALUES
('exclude_test_data_enabled', 'true');

INSERT INTO roles (code, name, description) VALUES
('TEST_ADMIN', 'Test Admin', 'Role for test admin'),
('TEST_USER', 'Test User', 'Role for test user');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);

INSERT INTO products (sku, name, description, price, currency, status) VALUES
('SKU-TEST-001', 'Test Product 1', 'Seed product 1', 100000, 'VND', 'ACTIVE'),
('SKU-TEST-002', 'Test Product 2', 'Seed product 2', 200000, 'VND', 'ACTIVE');

INSERT INTO inventory (product_id, quantity_on_hand, reserved, reorder_level) VALUES
(1, 50, 0, 10),
(2, 30, 0, 5);
