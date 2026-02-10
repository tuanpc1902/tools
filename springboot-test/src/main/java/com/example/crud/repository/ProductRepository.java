package com.example.crud.repository;

import com.example.crud.entity.Product;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Product createProduct(Product product) {
        String sql = "INSERT INTO products (sku, name, description, price, currency, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                now,
                now
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id).orElseThrow();
    }

    public Product updateProduct(Product product) {
        String sql = "UPDATE products SET sku = ?, name = ?, description = ?, price = ?, currency = ?, status = ?, " +
                "updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql,
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                LocalDateTime.now(),
                product.getId()
        );
        return findById(product.getId()).orElseThrow();
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT id, sku, name, description, price, currency, status, created_at, updated_at, deleted_at " +
                "FROM products WHERE id = ? AND deleted_at IS NULL";
        try {
            Product product = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Product.class), id);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Product> findBySku(String sku) {
        String sql = "SELECT id, sku, name, description, price, currency, status, created_at, updated_at, deleted_at " +
                "FROM products WHERE sku = ? AND deleted_at IS NULL";
        try {
            Product product = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Product.class), sku);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Product> findAll() {
        String sql = "SELECT id, sku, name, description, price, currency, status, created_at, updated_at, deleted_at " +
                "FROM products WHERE deleted_at IS NULL ORDER BY id";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Product.class));
    }

    public List<Product> searchByName(String name) {
        String sql = "SELECT id, sku, name, description, price, currency, status, created_at, updated_at, deleted_at " +
                "FROM products WHERE deleted_at IS NULL AND LOWER(name) LIKE LOWER(?) ORDER BY name";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Product.class), "%" + name + "%");
    }

    public boolean existsBySku(String sku) {
        String sql = "SELECT COUNT(*) FROM products WHERE sku = ? AND deleted_at IS NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sku);
        return count != null && count > 0;
    }

    public void softDelete(Long id) {
        String sql = "UPDATE products SET deleted_at = ?, status = 'INACTIVE', updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, now, now, id);
    }
}
