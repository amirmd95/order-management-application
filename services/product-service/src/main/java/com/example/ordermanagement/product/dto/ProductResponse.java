package com.example.ordermanagement.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
