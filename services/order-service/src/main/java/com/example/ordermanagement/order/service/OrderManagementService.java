package com.example.ordermanagement.order.service;

import com.example.ordermanagement.order.client.InventoryReservationClientRequest;
import com.example.ordermanagement.order.client.InventoryServiceClient;
import com.example.ordermanagement.order.client.ProductCatalogResponse;
import com.example.ordermanagement.order.client.ProductServiceClient;
import com.example.ordermanagement.order.dto.OrderItemResponse;
import com.example.ordermanagement.order.dto.OrderResponse;
import com.example.ordermanagement.order.dto.PlaceOrderItemRequest;
import com.example.ordermanagement.order.dto.PlaceOrderRequest;
import com.example.ordermanagement.order.entity.CustomerOrder;
import com.example.ordermanagement.order.entity.OrderLineItem;
import com.example.ordermanagement.order.entity.OrderStatus;
import com.example.ordermanagement.order.repository.CustomerOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderManagementService {

    private final CustomerOrderRepository customerOrderRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderManagementService(CustomerOrderRepository customerOrderRepository,
                                  ProductServiceClient productServiceClient,
                                  InventoryServiceClient inventoryServiceClient) {
        this.customerOrderRepository = customerOrderRepository;
        this.productServiceClient = productServiceClient;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        String normalizedCurrency = request.currency().toUpperCase();
        String orderNumber = generateOrderNumber();
        List<InventoryReservationClientRequest> reservations = new ArrayList<>();

        try {
            CustomerOrder order = new CustomerOrder();
            order.setOrderNumber(orderNumber);
            order.setCustomerEmail(request.customerEmail());
            order.setCurrency(normalizedCurrency);
            order.setStatus(OrderStatus.CONFIRMED);

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (PlaceOrderItemRequest itemRequest : request.items()) {
                ProductCatalogResponse product = productServiceClient.getProductBySku(itemRequest.sku());
                validateProduct(product, normalizedCurrency);

                InventoryReservationClientRequest reservation = new InventoryReservationClientRequest(
                        itemRequest.sku(),
                        itemRequest.quantity(),
                        orderNumber + "-" + itemRequest.sku()
                );
                inventoryServiceClient.reserveStock(reservation);
                reservations.add(reservation);

                BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(itemRequest.quantity()));
                totalAmount = totalAmount.add(lineTotal);

                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setSku(product.sku());
                lineItem.setProductName(product.name());
                lineItem.setQuantity(itemRequest.quantity());
                lineItem.setUnitPrice(product.price());
                lineItem.setLineTotal(lineTotal);
                order.addLineItem(lineItem);
            }

            order.setTotalAmount(totalAmount);
            return toResponse(customerOrderRepository.save(order));
        } catch (RuntimeException ex) {
            releaseReservations(reservations);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return customerOrderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return customerOrderRepository.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void validateProduct(ProductCatalogResponse product, String currency) {
        if (product == null || !product.active()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is inactive or unavailable");
        }

        if (!currency.equalsIgnoreCase(product.currency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All products must match the order currency");
        }
    }

    private void releaseReservations(List<InventoryReservationClientRequest> reservations) {
        for (int index = reservations.size() - 1; index >= 0; index--) {
            InventoryReservationClientRequest reservation = reservations.get(index);
            try {
                inventoryServiceClient.releaseStock(reservation);
            } catch (RuntimeException ignored) {
                // Best-effort compensation to avoid orphaned reservations.
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getLineItems()
                .stream()
                .map(lineItem -> new OrderItemResponse(
                        lineItem.getSku(),
                        lineItem.getProductName(),
                        lineItem.getQuantity(),
                        lineItem.getUnitPrice(),
                        lineItem.getLineTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getCurrency(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}
