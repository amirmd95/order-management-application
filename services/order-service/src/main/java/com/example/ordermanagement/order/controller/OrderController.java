package com.example.ordermanagement.order.controller;

import com.example.ordermanagement.order.dto.OrderResponse;
import com.example.ordermanagement.order.dto.PlaceOrderRequest;
import com.example.ordermanagement.order.service.OrderManagementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderManagementService orderManagementService;

    public OrderController(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderManagementService.placeOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderManagementService.getOrders();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId) {
        return orderManagementService.getOrder(orderId);
    }
}
