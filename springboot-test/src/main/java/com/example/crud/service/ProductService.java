package com.example.crud.service;

import com.example.crud.dto.InventoryDTO;
import com.example.crud.dto.ProductDTO;
import com.example.crud.entity.AuditLog;
import com.example.crud.entity.Inventory;
import com.example.crud.entity.Product;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.InventoryRepository;
import com.example.crud.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    public ProductDTO createProduct(ProductDTO dto, InventoryDTO inventoryDTO) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new ConflictException("SKU đã tồn tại: " + dto.getSku());
        }
        Product created = productRepository.createProduct(toEntity(dto));
        Inventory inventory = new Inventory(
                created.getId(),
                inventoryDTO.getQuantityOnHand(),
                inventoryDTO.getReserved(),
                inventoryDTO.getReorderLevel(),
                null
        );
        inventoryRepository.createInventory(inventory);
        auditLogService.record(new AuditLog(null, null, "CREATE", "PRODUCT", created.getId(), null, null, null, null));
        return toDTO(created);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy product với ID: " + id));

        Product bySku = productRepository.findBySku(dto.getSku()).orElse(null);
        if (bySku != null && !bySku.getId().equals(id)) {
            throw new ConflictException("SKU đã được dùng: " + dto.getSku());
        }

        existing.setSku(dto.getSku());
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setCurrency(dto.getCurrency());
        existing.setStatus(dto.getStatus());
        Product updated = productRepository.updateProduct(existing);
        auditLogService.record(new AuditLog(null, null, "UPDATE", "PRODUCT", updated.getId(), null, null, null, null));
        return toDTO(updated);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProduct(Long id) {
        return productRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy product với ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String name) {
        return productRepository.searchByName(name).stream().map(this::toDTO).toList();
    }

    public void deleteProduct(Long id) {
        if (productRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Không tìm thấy product với ID: " + id);
        }
        productRepository.softDelete(id);
        auditLogService.record(new AuditLog(null, null, "DELETE", "PRODUCT", id, null, null, null, null));
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCurrency(dto.getCurrency());
        product.setStatus(dto.getStatus());
        return product;
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        dto.setStatus(product.getStatus());
        return dto;
    }
}
