package com.example.inventoryorder.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String customerName) {
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void addLine(Product product, int quantity) {
        OrderLine line = new OrderLine(this, product, quantity, product.getPrice());
        this.lines.add(line);
        this.totalAmount = this.totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public void confirm() {
        requireStatus(OrderStatus.PENDING);
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        requireStatus(OrderStatus.PENDING);
        this.status = OrderStatus.CANCELLED;
    }

    public void markReturned() {
        requireStatus(OrderStatus.CONFIRMED);
        this.status = OrderStatus.RETURNED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order must be " + expected + " but was " + status);
        }
    }
}
