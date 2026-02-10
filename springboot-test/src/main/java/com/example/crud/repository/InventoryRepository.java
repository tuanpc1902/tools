package com.example.crud.repository;

import com.example.crud.entity.Inventory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InventoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Inventory createInventory(Inventory inventory) {
        String sql = "INSERT INTO inventory (product_id, quantity_on_hand, reserved, reorder_level, updated_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql,
                inventory.getProductId(),
                inventory.getQuantityOnHand(),
                inventory.getReserved(),
                inventory.getReorderLevel()
        );
        return findByProductId(inventory.getProductId()).orElseThrow();
    }

    public Optional<Inventory> findByProductId(Long productId) {
        String sql = "SELECT product_id, quantity_on_hand, reserved, reorder_level, updated_at FROM inventory WHERE product_id = ?";
        try {
            Inventory inventory = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(Inventory.class), productId);
            return Optional.ofNullable(inventory);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void adjustInventory(Long productId, int onHandDelta, int reservedDelta) {
        String sql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand + ?, reserved = reserved + ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        jdbcTemplate.update(sql, onHandDelta, reservedDelta, productId);
    }

    public void updateInventory(Inventory inventory) {
        String sql = "UPDATE inventory SET quantity_on_hand = ?, reserved = ?, reorder_level = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE product_id = ?";
        jdbcTemplate.update(sql,
                inventory.getQuantityOnHand(),
                inventory.getReserved(),
                inventory.getReorderLevel(),
                inventory.getProductId()
        );
    }
}
