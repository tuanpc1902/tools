package com.example.crud.repository;

import com.example.crud.entity.Order;
import com.example.crud.entity.OrderItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (order_number, user_id, status, total_amount, currency, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                now,
                now
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id).orElseThrow();
    }

    public void addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT id, order_number, user_id, status, total_amount, currency, created_at, updated_at " +
                "FROM orders WHERE id = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Order.class), id);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT id, order_number, user_id, status, total_amount, currency, created_at, updated_at " +
                "FROM orders WHERE user_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Order.class), userId);
    }

    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price, line_total FROM order_items WHERE order_id = ?";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(OrderItem.class), orderId);
    }

    public void updateOrderStatus(Long id, String status) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, LocalDateTime.now(), id);
    }

    public void updateTotalAmount(Long id, java.math.BigDecimal total) {
        String sql = "UPDATE orders SET total_amount = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, total, LocalDateTime.now(), id);
    }
}
