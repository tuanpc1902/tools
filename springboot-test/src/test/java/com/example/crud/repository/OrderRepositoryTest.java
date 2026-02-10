package com.example.crud.repository;

import com.example.crud.entity.Order;
import com.example.crud.entity.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(OrderRepository.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Create order and items - success")
    void createOrderAndItems() {
        jdbcTemplate.update("INSERT INTO users (name, email, phone, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                "Order User", "orderuser@example.com", "0909009009", "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, "orderuser@example.com");

        jdbcTemplate.update("INSERT INTO products (sku, name, description, price, currency, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "SKU-ORDER-01", "Order Product", "Desc", new BigDecimal("100000"), "VND", "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        Long productId = jdbcTemplate.queryForObject("SELECT id FROM products WHERE sku = ?", Long.class, "SKU-ORDER-01");

        Order order = new Order(null, "ORD-TEST-01", userId, "PENDING", new BigDecimal("100000"), "VND", LocalDateTime.now(), LocalDateTime.now());
        Order saved = orderRepository.createOrder(order);

        OrderItem item = new OrderItem(null, saved.getId(), productId, 1, new BigDecimal("100000"), new BigDecimal("100000"));
        orderRepository.addOrderItem(item);

        assertTrue(orderRepository.findById(saved.getId()).isPresent());
        assertEquals(1, orderRepository.findItemsByOrderId(saved.getId()).size());
    }
}
