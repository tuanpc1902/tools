package com.example.crud.repository;

import com.example.crud.entity.Role;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Role createRole(Role role) {
        String sql = "INSERT INTO roles (code, name, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, role.getCode(), role.getName(), role.getDescription(), now, now);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id).orElseThrow();
    }

    public Role updateRole(Role role) {
        String sql = "UPDATE roles SET code = ?, name = ?, description = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, role.getCode(), role.getName(), role.getDescription(), LocalDateTime.now(), role.getId());
        return findById(role.getId()).orElseThrow();
    }

    public Optional<Role> findById(Long id) {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM roles WHERE id = ?";
        try {
            Role role = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Role.class), id);
            return Optional.ofNullable(role);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Role> findByCode(String code) {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM roles WHERE code = ?";
        try {
            Role role = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Role.class), code);
            return Optional.ofNullable(role);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Role> findAll() {
        String sql = "SELECT id, code, name, description, created_at, updated_at FROM roles ORDER BY id";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Role.class));
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM roles WHERE id = ?", id);
    }
}
