package com.example.inventoryorder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    private Product product;
    private int availableQuantity;
    private int reservedQuantity;
    @Version
    private long version;

    protected InventoryItem() {
    }

    public InventoryItem(Product product, int availableQuantity) {
        this.product = product;
        this.availableQuantity = availableQuantity;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public long getVersion() {
        return version;
    }

    public void addStock(int quantity) {
        this.availableQuantity += quantity;
    }

    public void reserve(int quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + product.getSku());
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void confirmReservation(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Insufficient reserved stock for product " + product.getSku());
        }
        this.reservedQuantity -= quantity;
    }

    public void releaseReservation(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Insufficient reserved stock for product " + product.getSku());
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    public void restoreConfirmedStock(int quantity) {
        this.availableQuantity += quantity;
    }
}
