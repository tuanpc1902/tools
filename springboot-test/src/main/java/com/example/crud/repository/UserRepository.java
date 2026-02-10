package com.example.crud.repository;

import com.example.crud.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository sử dụng JdbcTemplate và Stored Procedures
 * Hỗ trợ dynamic queries và call stored procedures
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * CREATE - Tạo mới user
     */
    public User createUser(User user) {
        return createUserWithJdbcTemplate(user);
    }

    /**
     * CREATE - Tạo mới user sử dụng JdbcTemplate (alternative)
     */
    public User createUserWithJdbcTemplate(User user) {
        String sql = "INSERT INTO users (name, email, phone, status, created_at, updated_at) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
        
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, 
            user.getName(), 
            user.getEmail(), 
            user.getPhone(), 
            user.getStatus(), 
            now, 
            now);

        // Lấy ID vừa insert
        String getIdSql = "SELECT LAST_INSERT_ID()";
        Long id = jdbcTemplate.queryForObject(getIdSql, Long.class);
        user.setId(id);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        return user;
    }

    /**
     * READ - Lấy user theo ID
     */
    public Optional<User> getUserById(Long id) {
        return getUserByIdWithJdbcTemplate(id);
    }

    /**
     * READ - Lấy user theo ID sử dụng JdbcTemplate (alternative)
     */
    public Optional<User> getUserByIdWithJdbcTemplate(Long id) {
        String sql = "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at " +
                 "FROM users WHERE id = ? AND deleted_at IS NULL";
        
        try {
            User user = jdbcTemplate.queryForObject(sql, 
                BeanPropertyRowMapper.newInstance(User.class), 
                id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * READ ALL - Lấy tất cả users
     */
    public List<User> getAllUsers() {
        return getAllUsersWithJdbcTemplate();
    }

    /**
     * READ ALL - Lấy tất cả users sử dụng JdbcTemplate (alternative)
     */
    public List<User> getAllUsersWithJdbcTemplate() {
        String sql = "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at " +
                 "FROM users WHERE deleted_at IS NULL ORDER BY id";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(User.class));
    }

    /**
     * UPDATE - Cập nhật user
     */
    public User updateUser(User user) {
        return updateUserWithJdbcTemplate(user);
    }

    /**
     * UPDATE - Cập nhật user sử dụng JdbcTemplate (alternative)
     */
    public User updateUserWithJdbcTemplate(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, status = ?, updated_at = ? " +
                 "WHERE id = ? AND deleted_at IS NULL";
        
        jdbcTemplate.update(sql, 
            user.getName(), 
            user.getEmail(), 
            user.getPhone(), 
            user.getStatus(), 
            LocalDateTime.now(), 
            user.getId());
        
        return getUserById(user.getId()).orElseThrow();
    }

    /**
     * DELETE - Xóa user
     */
    public void deleteUser(Long id) {
        deleteUserWithJdbcTemplate(id);
    }

    /**
     * DELETE - Xóa user sử dụng JdbcTemplate (alternative)
     */
    public void deleteUserWithJdbcTemplate(Long id) {
        String sql = "UPDATE users SET deleted_at = ?, status = 'INACTIVE', updated_at = ? " +
                 "WHERE id = ? AND deleted_at IS NULL";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, now, now, id);
    }

    /**
     * Tìm user theo email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at " +
                 "FROM users WHERE email = ? AND deleted_at IS NULL";
        
        try {
            User user = jdbcTemplate.queryForObject(sql, 
                BeanPropertyRowMapper.newInstance(User.class), 
                email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND deleted_at IS NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Tìm users theo tên (dynamic query với NamedParameterJdbcTemplate)
     */
    public List<User> findByNameContaining(String name) {
        String sql = "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at " +
                 "FROM users WHERE LOWER(name) LIKE LOWER(:name) AND deleted_at IS NULL " +
                 "ORDER BY name";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", "%" + name + "%");
        
        return namedParameterJdbcTemplate.query(sql, params, 
            BeanPropertyRowMapper.newInstance(User.class));
    }

    /**
     * Dynamic query - Tìm users với nhiều điều kiện linh động
     */
    public List<User> findUsersDynamic(String name, String email, String phone) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at FROM users WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(name) LIKE LOWER(:name)");
            params.addValue("name", "%" + name + "%");
        }

        if (email != null && !email.isEmpty()) {
            sql.append(" AND email LIKE :email");
            params.addValue("email", "%" + email + "%");
        }

        if (phone != null && !phone.isEmpty()) {
            sql.append(" AND phone LIKE :phone");
            params.addValue("phone", "%" + phone + "%");
        }

        sql.append(" AND deleted_at IS NULL ORDER BY id");

        return namedParameterJdbcTemplate.query(sql.toString(), params, 
            BeanPropertyRowMapper.newInstance(User.class));
    }

    /**
     * Đếm số lượng users
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    /**
     * Kiểm tra user có tồn tại không
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND deleted_at IS NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Pagination - Lấy users với phân trang
     */
    public List<User> findAllWithPagination(int page, int size) {
        int offset = page * size;
        String sql = "SELECT id, name, email, phone, status, created_at, updated_at, deleted_at " +
                 "FROM users WHERE deleted_at IS NULL ORDER BY id LIMIT ? OFFSET ?";
        
        return jdbcTemplate.query(sql, 
            BeanPropertyRowMapper.newInstance(User.class), 
            size, 
            offset);
    }
}
