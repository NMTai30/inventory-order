package com.example.inventoryorder.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class InventoryDtos {
    private InventoryDtos() {
    }

    public record StockAdjustmentRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record InventoryResponse(Long productId, String sku, String name, int availableQuantity, int reservedQuantity) {
    }
}
