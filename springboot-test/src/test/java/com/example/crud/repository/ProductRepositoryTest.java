package com.example.crud.repository;

import com.example.crud.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(ProductRepository.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Create product - success")
    void createProductSuccess() {
        Product product = new Product(null, "SKU-PR-01", "Keyboard", "Mechanical", new BigDecimal("1500000"), "VND", "ACTIVE", null, null, null);

        Product saved = productRepository.createProduct(product);

        assertNotNull(saved.getId());
        assertEquals("SKU-PR-01", saved.getSku());
    }

    @Test
    @DisplayName("Search product by name")
    void searchByName() {
        Product product = new Product(null, "SKU-PR-02", "Mouse", "Wireless", new BigDecimal("500000"), "VND", "ACTIVE", null, null, null);
        productRepository.createProduct(product);

        assertFalse(productRepository.searchByName("mouse").isEmpty());
    }
}
