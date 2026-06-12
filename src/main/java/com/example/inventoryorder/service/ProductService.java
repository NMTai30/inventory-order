package com.example.inventoryorder.service;

import com.example.inventoryorder.api.dto.ProductDtos.ProductRequest;
import com.example.inventoryorder.api.dto.ProductDtos.ProductResponse;
import com.example.inventoryorder.domain.InventoryItem;
import com.example.inventoryorder.domain.Product;
import com.example.inventoryorder.repository.InventoryItemRepository;
import com.example.inventoryorder.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;
    private final InventoryItemRepository inventory;

    public ProductService(ProductRepository products, InventoryItemRepository inventory) {
        this.products = products;
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return products.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Pageable pageable) {
        String keyword = query == null ? "" : query.trim();
        Page<Product> page = keyword.isBlank()
                ? products.findAll(pageable)
                : products.findBySkuContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(findProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new IllegalArgumentException("SKU đã tồn tại");
        }
        Product product = products.save(new Product(request.sku(), request.name(), request.description(), request.price()));
        product.update(request.sku(), request.name(), request.description(), request.price(), isActive(request));
        inventory.save(new InventoryItem(product, 0));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        products.findBySku(request.sku())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("SKU đã tồn tại");
                });
        product.update(request.sku(), request.name(), request.description(), request.price(), isActive(request));
        return toResponse(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = findProduct(id);
        product.update(product.getSku(), product.getName(), product.getDescription(), product.getPrice(), false);
    }

    private Product findProduct(Long id) {
        return products.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));
    }

    private boolean isActive(ProductRequest request) {
        return request.active() == null || request.active();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(),
                product.getDescription(), product.getPrice(), product.isActive());
    }
}
