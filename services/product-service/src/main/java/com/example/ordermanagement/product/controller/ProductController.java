package com.example.ordermanagement.product.controller;

import com.example.ordermanagement.product.dto.ProductRequest;
import com.example.ordermanagement.product.dto.ProductResponse;
import com.example.ordermanagement.product.service.ProductCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productCatalogService.createProduct(request);
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return productCatalogService.getProducts();
    }

    @GetMapping("/{sku}")
    public ProductResponse getProductBySku(@PathVariable String sku) {
        return productCatalogService.getProductBySku(sku);
    }
}
