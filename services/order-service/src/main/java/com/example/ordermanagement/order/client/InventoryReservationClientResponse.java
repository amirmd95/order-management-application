package com.example.ordermanagement.order.client;

public record InventoryReservationClientResponse(
        String sku,
        int quantity,
        String reference,
        boolean reserved
) {
}
