package com.example.crud.service;

import com.example.crud.dto.InventoryDTO;
import com.example.crud.dto.ProductDTO;
import com.example.crud.entity.Product;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.InventoryRepository;
import com.example.crud.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductService productService;

    private ProductDTO productDTO;
    private InventoryDTO inventoryDTO;
    private Product product;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO(1L, "SKU-001", "Test Product", "Desc", new BigDecimal("1000"), "VND", "ACTIVE");
        inventoryDTO = new InventoryDTO(1L, 10, 0, 5);
        product = new Product(1L, "SKU-001", "Test Product", "Desc", new BigDecimal("1000"), "VND", "ACTIVE", null, null, null);
    }

    @Test
    @DisplayName("Create product - success")
    void createProductSuccess() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.createProduct(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTO, inventoryDTO);

        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
        verify(inventoryRepository, times(1)).createInventory(any());
        verify(auditLogService, times(1)).record(any());
    }

    @Test
    @DisplayName("Create product - sku conflict")
    void createProductConflict() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(ConflictException.class, () -> productService.createProduct(productDTO, inventoryDTO));
        verify(productRepository, never()).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("Update product - not found")
    void updateProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.updateProduct(99L, productDTO));
        verify(productRepository, never()).updateProduct(any(Product.class));
    }
}
