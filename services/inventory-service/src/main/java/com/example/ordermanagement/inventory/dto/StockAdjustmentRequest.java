package com.example.ordermanagement.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotBlank @Size(max = 64) String sku,
        @Min(0) int quantity
) {
}
