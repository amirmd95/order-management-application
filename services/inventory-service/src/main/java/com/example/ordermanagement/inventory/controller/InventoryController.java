package com.example.ordermanagement.inventory.controller;

import com.example.ordermanagement.inventory.dto.InventoryItemResponse;
import com.example.ordermanagement.inventory.dto.InventoryReservationResponse;
import com.example.ordermanagement.inventory.dto.ReserveInventoryRequest;
import com.example.ordermanagement.inventory.dto.StockAdjustmentRequest;
import com.example.ordermanagement.inventory.service.InventoryManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryManagementService inventoryManagementService;

    public InventoryController(InventoryManagementService inventoryManagementService) {
        this.inventoryManagementService = inventoryManagementService;
    }

    @PostMapping("/stock")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse upsertStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryManagementService.upsertStock(request);
    }

    @GetMapping("/{sku}")
    public InventoryItemResponse getInventory(@PathVariable String sku) {
        return inventoryManagementService.getInventory(sku);
    }

    @PostMapping("/reservations")
    public InventoryReservationResponse reserveStock(@Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryManagementService.reserveStock(request);
    }

    @PostMapping("/reservations/release")
    public InventoryReservationResponse releaseStock(@Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryManagementService.releaseStock(request);
    }
}
