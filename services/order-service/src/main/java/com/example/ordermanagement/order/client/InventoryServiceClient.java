package com.example.ordermanagement.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;

    public InventoryServiceClient(RestClient.Builder restClientBuilder,
                                  @Value("${services.inventory-service.url}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(inventoryServiceUrl).build();
    }

    public InventoryReservationClientResponse reserveStock(InventoryReservationClientRequest request) {
        return sendReservationRequest("/api/inventory/reservations", request);
    }

    public InventoryReservationClientResponse releaseStock(InventoryReservationClientRequest request) {
        return sendReservationRequest("/api/inventory/reservations/release", request);
    }

    private InventoryReservationClientResponse sendReservationRequest(String uri,
                                                                      InventoryReservationClientRequest request) {
        try {
            InventoryReservationClientResponse response = restClient.post()
                    .uri(uri)
                    .body(request)
                    .retrieve()
                    .body(InventoryReservationClientResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Inventory service returned an empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()
                    || ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory reservation failed", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to reserve inventory");
        }
    }
}
