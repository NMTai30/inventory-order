package com.example.inventoryorder.repository;

import com.example.inventoryorder.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
}
