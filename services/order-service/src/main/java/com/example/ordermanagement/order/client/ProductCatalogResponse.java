package com.example.ordermanagement.order.client;

import java.math.BigDecimal;

public record ProductCatalogResponse(
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        boolean active
) {
}
