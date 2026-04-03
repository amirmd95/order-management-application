package com.example.ordermanagement.order.repository;

import com.example.ordermanagement.order.entity.CustomerOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
}
