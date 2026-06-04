package com.example.inventoryorder.api;

import com.example.inventoryorder.api.dto.OrderDtos.CreateOrderRequest;
import com.example.inventoryorder.api.dto.OrderDtos.OrderResponse;
import com.example.inventoryorder.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    List<OrderResponse> list() {
        return orders.list();
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) {
        return orders.get(id);
    }

    @PostMapping
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orders.create(request);
    }

    @PostMapping("/{id}/confirm")
    OrderResponse confirm(@PathVariable Long id) {
        return orders.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable Long id) {
        return orders.cancel(id);
    }

    @PostMapping("/{id}/return")
    OrderResponse returnOrder(@PathVariable Long id) {
        return orders.returnOrder(id);
    }
}
