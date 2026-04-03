package com.example.ordermanagement.order.client;

public record InventoryReservationClientRequest(
        String sku,
        int quantity,
        String reference
) {
}
