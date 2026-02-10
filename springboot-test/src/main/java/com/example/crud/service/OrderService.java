package com.example.crud.service;

import com.example.crud.dto.OrderCreateRequest;
import com.example.crud.dto.OrderItemDTO;
import com.example.crud.dto.OrderItemRequest;
import com.example.crud.dto.OrderResponse;
import com.example.crud.entity.AuditLog;
import com.example.crud.entity.Inventory;
import com.example.crud.entity.Order;
import com.example.crud.entity.OrderItem;
import com.example.crud.entity.Product;
import com.example.crud.exception.BadRequestException;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.InventoryRepository;
import com.example.crud.repository.OrderRepository;
import com.example.crud.repository.ProductRepository;
import com.example.crud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        UserRepository userRepository,
                        AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public OrderResponse createOrder(OrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new NotFoundException("Không tìm thấy user với ID: " + request.getUserId());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy product với ID: " + item.getProductId()));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new ConflictException("Sản phẩm không ở trạng thái ACTIVE: " + product.getSku());
            }

            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy inventory cho product: " + product.getId()));

            int available = inventory.getQuantityOnHand() - inventory.getReserved();
            if (available < item.getQuantity()) {
                throw new ConflictException("Không đủ tồn kho cho SKU: " + product.getSku());
            }

            inventoryRepository.adjustInventory(product.getId(), 0, item.getQuantity());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.getUserId());
        order.setStatus("PENDING");
        order.setTotalAmount(total);
        order.setCurrency(request.getCurrency() != null ? request.getCurrency() : "VND");

        Order created = orderRepository.createOrder(order);

        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            OrderItem orderItem = new OrderItem(null, created.getId(), product.getId(), item.getQuantity(), product.getPrice(), lineTotal);
            orderRepository.addOrderItem(orderItem);
            inventoryRepository.adjustInventory(product.getId(), -item.getQuantity(), -item.getQuantity());
        }

        auditLogService.record(new AuditLog(null, request.getUserId(), "CREATE", "ORDER", created.getId(), null, null, null, null));
        return getOrder(created.getId());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order với ID: " + id));

        List<OrderItemDTO> items = orderRepository.findItemsByOrderId(order.getId()).stream()
                .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + userId);
        }
        return orderRepository.findByUserId(userId).stream().map(order -> {
            List<OrderItemDTO> items = orderRepository.findItemsByOrderId(order.getId()).stream()
                    .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
                    .toList();
            return new OrderResponse(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getUserId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getCurrency(),
                    items,
                    order.getCreatedAt(),
                    order.getUpdatedAt()
            );
        }).toList();
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order với ID: " + id));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Đơn hàng đã bị hủy");
        }
        if ("SHIPPED".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Không thể hủy đơn đã giao");
        }

        List<OrderItem> items = orderRepository.findItemsByOrderId(order.getId());
        for (OrderItem item : items) {
            inventoryRepository.adjustInventory(item.getProductId(), item.getQuantity(), 0);
        }

        orderRepository.updateOrderStatus(order.getId(), "CANCELLED");
        auditLogService.record(new AuditLog(null, order.getUserId(), "CANCEL", "ORDER", order.getId(), null, null, null, null));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long timestamp = System.currentTimeMillis() % 100000;
        return "ORD-" + date + "-" + String.format("%05d", timestamp);
    }
}
