package com.example.inventoryorder.config;

import com.example.inventoryorder.domain.AppUser;
import com.example.inventoryorder.domain.InventoryItem;
import com.example.inventoryorder.domain.Product;
import com.example.inventoryorder.repository.InventoryItemRepository;
import com.example.inventoryorder.repository.ProductRepository;
import com.example.inventoryorder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(UserRepository users,
                               ProductRepository products,
                               InventoryItemRepository inventory,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (users.count() == 0) {
                users.save(new AppUser("admin", passwordEncoder.encode("admin123"), Set.of("ROLE_ADMIN", "ROLE_USER")));
                users.save(new AppUser("user", passwordEncoder.encode("user123"), Set.of("ROLE_USER")));
            }

            if (products.count() == 0) {
                Product keyboard = products.save(new Product("SKU-KEYBOARD", "Mechanical Keyboard", "Starter sample product", new BigDecimal("79.90")));
                Product mouse = products.save(new Product("SKU-MOUSE", "Wireless Mouse", "Starter sample product", new BigDecimal("29.90")));
                inventory.save(new InventoryItem(keyboard, 100));
                inventory.save(new InventoryItem(mouse, 150));
            }
        };
    }
}
