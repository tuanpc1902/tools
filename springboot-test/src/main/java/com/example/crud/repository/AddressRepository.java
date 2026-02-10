package com.example.crud.repository;

import com.example.crud.entity.Address;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepository {

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Address createAddress(Address address) {
        String sql = "INSERT INTO addresses (user_id, type, line1, line2, city, state, postal_code, country, is_default, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                address.getUserId(),
                address.getType(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                now,
                now
        );

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id).orElseThrow();
    }

    public Address updateAddress(Address address) {
        String sql = "UPDATE addresses SET type = ?, line1 = ?, line2 = ?, city = ?, state = ?, postal_code = ?, " +
                "country = ?, is_default = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql,
                address.getType(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                LocalDateTime.now(),
                address.getId()
        );

        return findById(address.getId()).orElseThrow();
    }

    public Optional<Address> findById(Long id) {
        String sql = "SELECT id, user_id, type, line1, line2, city, state, postal_code, country, is_default, " +
                "created_at, updated_at, deleted_at FROM addresses WHERE id = ? AND deleted_at IS NULL";
        try {
            Address address = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Address.class), id);
            return Optional.ofNullable(address);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Address> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, type, line1, line2, city, state, postal_code, country, is_default, " +
                "created_at, updated_at, deleted_at FROM addresses WHERE user_id = ? AND deleted_at IS NULL ORDER BY id";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Address.class), userId);
    }

    public void clearDefaultForUser(Long userId) {
        String sql = "UPDATE addresses SET is_default = 0, updated_at = ? WHERE user_id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, LocalDateTime.now(), userId);
    }

    public void softDelete(Long id) {
        String sql = "UPDATE addresses SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, now, now, id);
    }
}
