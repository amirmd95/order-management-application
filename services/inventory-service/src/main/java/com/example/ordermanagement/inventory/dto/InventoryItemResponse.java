package com.example.ordermanagement.inventory.dto;

import java.time.OffsetDateTime;

public record InventoryItemResponse(
        String sku,
        int availableQuantity,
        int reservedQuantity,
        OffsetDateTime updatedAt
) {
}
