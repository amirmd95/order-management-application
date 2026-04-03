package com.example.ordermanagement.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
