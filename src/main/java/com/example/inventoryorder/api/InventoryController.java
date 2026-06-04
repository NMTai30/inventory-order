package com.example.inventoryorder.api;

import com.example.inventoryorder.api.dto.InventoryDtos.InventoryResponse;
import com.example.inventoryorder.api.dto.InventoryDtos.StockAdjustmentRequest;
import com.example.inventoryorder.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventory;

    public InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    List<InventoryResponse> list() {
        return inventory.list();
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    InventoryResponse addStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return inventory.addStock(request.productId(), request.quantity());
    }
}
