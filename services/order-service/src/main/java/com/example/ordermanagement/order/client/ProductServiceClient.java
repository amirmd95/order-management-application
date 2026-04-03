package com.example.ordermanagement.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${services.product-service.url}") String productServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(productServiceUrl).build();
    }

    public ProductCatalogResponse getProductBySku(String sku) {
        try {
            ProductCatalogResponse response = restClient.get()
                    .uri("/api/products/{sku}", sku)
                    .retrieve()
                    .body(ProductCatalogResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service returned an empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown product SKU: " + sku);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to fetch product data");
        }
    }
}
