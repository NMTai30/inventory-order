package com.example.inventoryorder;

import com.example.inventoryorder.api.dto.OrderDtos.CreateOrderRequest;
import com.example.inventoryorder.api.dto.OrderDtos.OrderLineRequest;
import com.example.inventoryorder.domain.OrderStatus;
import com.example.inventoryorder.repository.InventoryItemRepository;
import com.example.inventoryorder.repository.ProductRepository;
import com.example.inventoryorder.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceTest {
    @Autowired
    OrderService orders;
    @Autowired
    ProductRepository products;
    @Autowired
    InventoryItemRepository inventory;

    @Test
    void createConfirmAndReturnOrderMovesStockCorrectly() {
        var product = products.findBySku("SKU-KEYBOARD").orElseThrow();
        var created = orders.create(new CreateOrderRequest("Acme", List.of(new OrderLineRequest(product.getId(), 2))));
        var afterReserve = inventory.findByProductId(product.getId()).orElseThrow();

        assertThat(created.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(afterReserve.getAvailableQuantity()).isEqualTo(98);
        assertThat(afterReserve.getReservedQuantity()).isEqualTo(2);

        var confirmed = orders.confirm(created.id());
        var afterConfirm = inventory.findByProductId(product.getId()).orElseThrow();

        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(afterConfirm.getAvailableQuantity()).isEqualTo(98);
        assertThat(afterConfirm.getReservedQuantity()).isEqualTo(0);

        var returned = orders.returnOrder(created.id());
        var afterReturn = inventory.findByProductId(product.getId()).orElseThrow();

        assertThat(returned.status()).isEqualTo(OrderStatus.RETURNED);
        assertThat(afterReturn.getAvailableQuantity()).isEqualTo(100);
    }
}
