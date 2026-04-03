package com.example.ordermanagement.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        String customerEmail,
        String currency,
        String status,
        BigDecimal totalAmount,
        OffsetDateTime createdAt,
        List<OrderItemResponse> items
) {
}
