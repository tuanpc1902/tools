# Hướng Dẫn Setup MySQL và Stored Procedures

## 📋 Yêu Cầu

- MySQL 8.0+ hoặc MariaDB 10.3+
- Java 17+
- Maven 3.6+

## 🗄️ Setup Database

### 1. Tạo Database và Tables

Chạy file `src/main/resources/db/schema.sql`:

```sql
-- Tạo database
CREATE DATABASE IF NOT EXISTS crud_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE crud_db;

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Tạo Stored Procedures

Chạy file `src/main/resources/db/stored-procedures.sql` để tạo tất cả stored procedures:

```bash
mysql -u root -p crud_db < src/main/resources/db/stored-procedures.sql
```

Hoặc copy và paste vào MySQL client.

### 3. Cấu Hình Application

Cập nhật `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=your_password
```

## 🚀 Chạy Application

```bash
# Build project
mvn clean install

# Chạy application
mvn spring-boot:run
```

## 📝 Kiểm Tra Stored Procedures

Sau khi tạo stored procedures, kiểm tra bằng cách:

```sql
-- Xem danh sách stored procedures
SHOW PROCEDURE STATUS WHERE Db = 'crud_db';

-- Xem chi tiết một stored procedure
SHOW CREATE PROCEDURE sp_create_user;
```

## 🧪 Testing

Tests sử dụng H2 in-memory database (nhanh hơn MySQL):

```bash
mvn test
```

## 📚 Các Stored Procedures Đã Tạo

1. **sp_create_user** - Tạo mới user
2. **sp_get_user_by_id** - Lấy user theo ID
3. **sp_get_all_users** - Lấy tất cả users
4. **sp_update_user** - Cập nhật user
5. **sp_delete_user** - Xóa user
6. **sp_search_users** - Tìm kiếm users với nhiều điều kiện
7. **sp_get_user_count** - Đếm số lượng users

## 🔧 Troubleshooting

### Lỗi kết nối MySQL

- Kiểm tra MySQL đã chạy chưa: `mysql --version`
- Kiểm tra username/password trong `application.properties`
- Kiểm tra port MySQL (mặc định 3306)

### Lỗi Stored Procedure không tồn tại

- Đảm bảo đã chạy file `stored-procedures.sql`
- Kiểm tra database name đúng không
- Kiểm tra user có quyền CREATE PROCEDURE

### Lỗi Character Encoding

- Đảm bảo database sử dụng `utf8mb4`
- Thêm `?characterEncoding=utf8mb4` vào connection string
