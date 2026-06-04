package com.example.inventoryorder.api;

import com.example.inventoryorder.api.dto.ProductDtos.ProductRequest;
import com.example.inventoryorder.api.dto.ProductDtos.ProductResponse;
import com.example.inventoryorder.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    List<ProductResponse> list() {
        return products.list();
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable Long id) {
        return products.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return products.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return products.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void deactivate(@PathVariable Long id) {
        products.deactivate(id);
    }
}
