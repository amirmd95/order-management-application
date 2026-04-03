package com.example.ordermanagement.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReserveInventoryRequest(
        @NotBlank @Size(max = 64) String sku,
        @Min(1) int quantity,
        @NotBlank String reference
) {
}
