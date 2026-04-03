package com.example.ordermanagement.inventory.dto;

public record InventoryReservationResponse(
        String sku,
        int quantity,
        String reference,
        boolean reserved
) {
}
