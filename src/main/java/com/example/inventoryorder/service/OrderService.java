package com.example.inventoryorder.service;

import com.example.inventoryorder.api.dto.OrderDtos.CreateOrderRequest;
import com.example.inventoryorder.api.dto.OrderDtos.OrderLineResponse;
import com.example.inventoryorder.api.dto.OrderDtos.OrderResponse;
import com.example.inventoryorder.domain.CustomerOrder;
import com.example.inventoryorder.domain.InventoryItem;
import com.example.inventoryorder.domain.OrderLine;
import com.example.inventoryorder.domain.Product;
import com.example.inventoryorder.repository.InventoryItemRepository;
import com.example.inventoryorder.repository.OrderRepository;
import com.example.inventoryorder.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final InventoryItemRepository inventory;

    public OrderService(OrderRepository orders, ProductRepository products, InventoryItemRepository inventory) {
        this.orders = orders;
        this.products = products;
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder(request.customerName());
        for (var line : request.items()) {
            Product product = products.findById(line.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new EntityNotFoundException("Active product not found: " + line.productId()));
            InventoryItem item = lockInventory(product.getId());
            item.reserve(line.quantity());
            order.addLine(product, line.quantity());
        }
        return toResponse(orders.save(order));
    }

    @Transactional
    public OrderResponse confirm(Long id) {
        CustomerOrder order = findOrder(id);
        for (OrderLine line : order.getLines()) {
            lockInventory(line.getProduct().getId()).confirmReservation(line.getQuantity());
        }
        order.confirm();
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        CustomerOrder order = findOrder(id);
        for (OrderLine line : order.getLines()) {
            lockInventory(line.getProduct().getId()).releaseReservation(line.getQuantity());
        }
        order.cancel();
        return toResponse(order);
    }

    @Transactional
    public OrderResponse returnOrder(Long id) {
        CustomerOrder order = findOrder(id);
        for (OrderLine line : order.getLines()) {
            lockInventory(line.getProduct().getId()).restoreConfirmedStock(line.getQuantity());
        }
        order.markReturned();
        return toResponse(order);
    }

    private CustomerOrder findOrder(Long id) {
        return orders.findById(id).orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    private InventoryItem lockInventory(Long productId) {
        return inventory.lockByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found: " + productId));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getStatus(), order.getTotalAmount(),
                order.getCreatedAt(), order.getLines().stream().map(this::toLineResponse).toList());
    }

    private OrderLineResponse toLineResponse(OrderLine line) {
        Product product = line.getProduct();
        return new OrderLineResponse(product.getId(), product.getSku(), product.getName(),
                line.getQuantity(), line.getUnitPrice());
    }
}
