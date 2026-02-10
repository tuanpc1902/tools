package com.example.crud.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isEnabled(String configKey) {
        String sql = "SELECT config_value FROM app_config WHERE config_key = ?";
        try {
            String value = jdbcTemplate.queryForObject(sql, String.class, configKey);
            return value != null && "true".equalsIgnoreCase(value.trim());
        } catch (Exception e) {
            return false;
        }
    }
}
