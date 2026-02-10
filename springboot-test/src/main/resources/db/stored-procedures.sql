-- Stored Procedures cho User CRUD Operations
-- File này chứa tất cả các stored procedures cần thiết

USE crud_db;

-- ============================================
-- Stored Procedure: sp_create_user
-- Mục đích: Tạo mới user
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_create_user$$

CREATE PROCEDURE sp_create_user(
    IN p_name VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_phone VARCHAR(20),
    IN p_status VARCHAR(20),
    OUT p_user_id BIGINT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Kiểm tra email đã tồn tại chưa
    IF EXISTS (SELECT 1 FROM users WHERE email = p_email) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email đã tồn tại';
    END IF;

    -- Insert user mới
    INSERT INTO users (name, email, phone, status, created_at, updated_at)
    VALUES (
        p_name,
        p_email,
        p_phone,
        IFNULL(NULLIF(p_status, ''), 'ACTIVE'),
        NOW(),
        NOW()
    );

    -- Lấy ID vừa insert
    SET p_user_id = LAST_INSERT_ID();

    COMMIT;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_get_user_by_id
-- Mục đích: Lấy user theo ID
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_get_user_by_id$$

CREATE PROCEDURE sp_get_user_by_id(
    IN p_user_id BIGINT
)
BEGIN
    SELECT id, name, email, phone, status, level_code, is_test, created_at, updated_at, deleted_at
    FROM users
    WHERE id = p_user_id
      AND deleted_at IS NULL;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_get_all_users
-- Mục đích: Lấy tất cả users
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_get_all_users$$

CREATE PROCEDURE sp_get_all_users()
BEGIN
    SELECT id, name, email, phone, status, level_code, is_test, created_at, updated_at, deleted_at
    FROM users
    WHERE deleted_at IS NULL
    ORDER BY id;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_update_user
-- Mục đích: Cập nhật thông tin user
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_update_user$$

CREATE PROCEDURE sp_update_user(
    IN p_user_id BIGINT,
    IN p_name VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_phone VARCHAR(20),
    IN p_status VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Kiểm tra user có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = p_user_id AND deleted_at IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy user với ID này';
    END IF;

    -- Kiểm tra email có trùng với user khác không
    IF EXISTS (SELECT 1 FROM users WHERE email = p_email AND id != p_user_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email đã được sử dụng bởi user khác';
    END IF;

    -- Update user
    UPDATE users
    SET name = p_name,
        email = p_email,
        phone = p_phone,
        status = IFNULL(NULLIF(p_status, ''), status),
        updated_at = NOW()
    WHERE id = p_user_id;

    COMMIT;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_delete_user
-- Mục đích: Xóa user
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_delete_user$$

CREATE PROCEDURE sp_delete_user(
    IN p_user_id BIGINT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Kiểm tra user có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = p_user_id AND deleted_at IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy user với ID này';
    END IF;

    -- Soft delete user
    UPDATE users
    SET deleted_at = NOW(),
        status = 'INACTIVE',
        updated_at = NOW()
    WHERE id = p_user_id;

    COMMIT;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_search_users
-- Mục đích: Tìm kiếm users với nhiều điều kiện linh động
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_search_users$$

CREATE PROCEDURE sp_search_users(
    IN p_name VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_phone VARCHAR(20),
    IN p_page INT,
    IN p_size INT
)
BEGIN
    DECLARE v_offset INT DEFAULT 0;
    SET v_offset = p_page * p_size;

    SELECT id, name, email, phone, status, level_code, is_test, created_at, updated_at, deleted_at
    FROM users
    WHERE (p_name IS NULL OR p_name = '' OR LOWER(name) LIKE LOWER(CONCAT('%', p_name, '%')))
      AND (p_email IS NULL OR p_email = '' OR email LIKE CONCAT('%', p_email, '%'))
      AND (p_phone IS NULL OR p_phone = '' OR phone LIKE CONCAT('%', p_phone, '%'))
            AND deleted_at IS NULL
    ORDER BY id
    LIMIT p_size OFFSET v_offset;
END$$

DELIMITER ;

-- ============================================
-- Stored Procedure: sp_get_user_count
-- Mục đích: Đếm số lượng users với điều kiện
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_get_user_count$$

CREATE PROCEDURE sp_get_user_count(
    IN p_name VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_phone VARCHAR(20),
    OUT p_total BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_total
    FROM users
    WHERE (p_name IS NULL OR p_name = '' OR LOWER(name) LIKE LOWER(CONCAT('%', p_name, '%')))
      AND (p_email IS NULL OR p_email = '' OR email LIKE CONCAT('%', p_email, '%'))
            AND (p_phone IS NULL OR p_phone = '' OR phone LIKE CONCAT('%', p_phone, '%'))
            AND deleted_at IS NULL;
END$$

DELIMITER ;
