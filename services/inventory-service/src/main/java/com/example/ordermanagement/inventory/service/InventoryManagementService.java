package com.example.ordermanagement.inventory.service;

import com.example.ordermanagement.inventory.dto.InventoryItemResponse;
import com.example.ordermanagement.inventory.dto.InventoryReservationResponse;
import com.example.ordermanagement.inventory.dto.ReserveInventoryRequest;
import com.example.ordermanagement.inventory.dto.StockAdjustmentRequest;
import com.example.ordermanagement.inventory.entity.InventoryItem;
import com.example.ordermanagement.inventory.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryManagementService {

    private final InventoryRepository inventoryRepository;

    public InventoryManagementService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryItemResponse upsertStock(StockAdjustmentRequest request) {
        InventoryItem inventoryItem = inventoryRepository.findBySku(request.sku())
                .orElseGet(InventoryItem::new);

        inventoryItem.setSku(request.sku());
        inventoryItem.setAvailableQuantity(request.quantity());
        if (inventoryItem.getReservedQuantity() > request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reserved stock exceeds available quantity"
            );
        }

        return toResponse(inventoryRepository.save(inventoryItem));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventory(String sku) {
        return inventoryRepository.findBySku(sku)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));
    }

    @Transactional
    public InventoryReservationResponse reserveStock(ReserveInventoryRequest request) {
        InventoryItem inventoryItem = inventoryRepository.findBySku(request.sku())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));

        int sellable = inventoryItem.getAvailableQuantity() - inventoryItem.getReservedQuantity();
        if (sellable < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient inventory available");
        }

        inventoryItem.setReservedQuantity(inventoryItem.getReservedQuantity() + request.quantity());
        inventoryRepository.save(inventoryItem);

        return new InventoryReservationResponse(request.sku(), request.quantity(), request.reference(), true);
    }

    @Transactional
    public InventoryReservationResponse releaseStock(ReserveInventoryRequest request) {
        InventoryItem inventoryItem = inventoryRepository.findBySku(request.sku())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));

        int releasedQuantity = Math.min(request.quantity(), inventoryItem.getReservedQuantity());
        inventoryItem.setReservedQuantity(inventoryItem.getReservedQuantity() - releasedQuantity);
        inventoryRepository.save(inventoryItem);

        return new InventoryReservationResponse(request.sku(), releasedQuantity, request.reference(), false);
    }

    private InventoryItemResponse toResponse(InventoryItem inventoryItem) {
        return new InventoryItemResponse(
                inventoryItem.getSku(),
                inventoryItem.getAvailableQuantity(),
                inventoryItem.getReservedQuantity(),
                inventoryItem.getUpdatedAt()
        );
    }
}
