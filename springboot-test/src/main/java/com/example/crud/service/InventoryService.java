package com.example.crud.service;

import com.example.crud.dto.InventoryDTO;
import com.example.crud.entity.Inventory;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryDTO updateInventory(InventoryDTO dto) {
        Inventory inventory = inventoryRepository.findByProductId(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy inventory cho product: " + dto.getProductId()));
        inventory.setQuantityOnHand(dto.getQuantityOnHand());
        inventory.setReserved(dto.getReserved());
        inventory.setReorderLevel(dto.getReorderLevel());
        inventoryRepository.updateInventory(inventory);
        return toDTO(inventoryRepository.findByProductId(dto.getProductId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public InventoryDTO getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy inventory cho product: " + productId));
    }

    private InventoryDTO toDTO(Inventory inventory) {
        return new InventoryDTO(
                inventory.getProductId(),
                inventory.getQuantityOnHand(),
                inventory.getReserved(),
                inventory.getReorderLevel()
        );
    }
}
