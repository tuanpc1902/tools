package com.example.crud.service;

import com.example.crud.dto.OrderCreateRequest;
import com.example.crud.dto.OrderItemRequest;
import com.example.crud.entity.Inventory;
import com.example.crud.entity.Order;
import com.example.crud.entity.OrderItem;
import com.example.crud.entity.Product;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.InventoryRepository;
import com.example.crud.repository.OrderRepository;
import com.example.crud.repository.ProductRepository;
import com.example.crud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "SKU-001", "Test Product", "Desc", new BigDecimal("1000"), "VND", "ACTIVE", null, null, null);
        inventory = new Inventory(1L, 10, 0, 2, null);
    }

    @Test
    @DisplayName("Create order - success")
    void createOrderSuccess() {
        OrderCreateRequest request = new OrderCreateRequest(1L, "VND", List.of(new OrderItemRequest(1L, 2)));
        Order order = new Order(1L, "ORD-20260207-00001", 1L, "PENDING", new BigDecimal("2000"), "VND", LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(orderRepository.createOrder(any(Order.class))).thenReturn(order);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findItemsByOrderId(1L))
                .thenReturn(List.of(new OrderItem(1L, 1L, 1L, 2, new BigDecimal("1000"), new BigDecimal("2000"))));

        var response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(orderRepository, times(1)).addOrderItem(any(OrderItem.class));
        verify(inventoryRepository, atLeastOnce()).adjustInventory(eq(1L), anyInt(), anyInt());
        verify(auditLogService, times(1)).record(any());
    }

    @Test
    @DisplayName("Create order - insufficient inventory")
    void createOrderInsufficientInventory() {
        OrderCreateRequest request = new OrderCreateRequest(1L, "VND", List.of(new OrderItemRequest(1L, 20)));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        assertThrows(ConflictException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).createOrder(any(Order.class));
    }

    @Test
    @DisplayName("Get order - not found")
    void getOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrder(99L));
    }
}
