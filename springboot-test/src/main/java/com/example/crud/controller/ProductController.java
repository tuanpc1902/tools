package com.example.crud.controller;

import com.example.crud.dto.InventoryDTO;
import com.example.crud.dto.ProductDTO;
import com.example.crud.service.InventoryService;
import com.example.crud.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public ProductController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @RequestParam(defaultValue = "0") int quantity,
            @RequestParam(defaultValue = "0") int reserved,
            @RequestParam(defaultValue = "0") int reorderLevel) {
        InventoryDTO inventoryDTO = new InventoryDTO(null, quantity, reserved, reorderLevel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDTO, inventoryDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(@RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(name));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDTO inventoryDTO) {
        inventoryDTO.setProductId(id);
        return ResponseEntity.ok(inventoryService.updateInventory(inventoryDTO));
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventory(id));
    }
}
