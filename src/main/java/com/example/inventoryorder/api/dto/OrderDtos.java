package com.example.inventoryorder.api.dto;

import com.example.inventoryorder.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CreateOrderRequest(@NotBlank String customerName, @NotEmpty List<@Valid OrderLineRequest> items) {
    }

    public record OrderLineRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record OrderResponse(Long id,
                                String customerName,
                                OrderStatus status,
                                BigDecimal totalAmount,
                                Instant createdAt,
                                List<OrderLineResponse> items) {
    }

    public record OrderLineResponse(Long productId, String sku, String name, int quantity, BigDecimal unitPrice) {
    }
}
