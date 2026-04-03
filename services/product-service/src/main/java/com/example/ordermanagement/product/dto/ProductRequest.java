package com.example.ordermanagement.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 300) String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        boolean active
) {
}
