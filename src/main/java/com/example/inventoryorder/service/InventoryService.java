package com.example.inventoryorder.service;

import com.example.inventoryorder.api.dto.InventoryDtos.InventoryResponse;
import com.example.inventoryorder.domain.InventoryItem;
import com.example.inventoryorder.repository.InventoryItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryItemRepository inventory;

    public InventoryService(InventoryItemRepository inventory) {
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> list() {
        return inventory.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponse addStock(Long productId, int quantity) {
        InventoryItem item = inventory.lockByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));
        item.addStock(quantity);
        return toResponse(item);
    }

    private InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(item.getProduct().getId(), item.getProduct().getSku(), item.getProduct().getName(),
                item.getAvailableQuantity(), item.getReservedQuantity());
    }
}
