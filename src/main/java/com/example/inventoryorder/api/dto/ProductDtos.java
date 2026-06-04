package com.example.inventoryorder.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String sku,
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            Boolean active) {
    }

    public record ProductResponse(Long id, String sku, String name, String description, BigDecimal price, boolean active) {
    }
}
