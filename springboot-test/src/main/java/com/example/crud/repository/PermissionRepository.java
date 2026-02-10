package com.example.crud.repository;

import com.example.crud.entity.Permission;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PermissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Permission createPermission(Permission permission) {
        String sql = "INSERT INTO permissions (code, name, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, permission.getCode(), permission.getName(), permission.getDescription(), now, now);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id).orElseThrow();
    }

    public Permission updatePermission(Permission permission) {
        String sql = "UPDATE permissions SET code = ?, name = ?, description = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, permission.getCode(), permission.getName(), permission.getDescription(), LocalDateTime.now(), permission.getId());
        return findById(permission.getId()).orElseThrow();
    }

    public Optional<Permission> findById(Long id) {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM permissions WHERE id = ?";
        try {
            Permission permission = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Permission.class), id);
            return Optional.ofNullable(permission);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Permission> findByCode(String code) {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM permissions WHERE code = ?";
        try {
            Permission permission = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Permission.class), code);
            return Optional.ofNullable(permission);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Permission> findAll() {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM permissions ORDER BY id";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Permission.class));
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM permissions WHERE id = ?", id);
    }
}
