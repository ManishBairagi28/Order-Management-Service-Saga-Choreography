package com.orderms.inventory.controller;

import com.orderms.inventory.dto.CreateProductRequest;
import com.orderms.inventory.dto.ProductResponse;
import com.orderms.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());
        return productService.createProduct(request);
    }

    @GetMapping
    public Flux<ProductResponse> getAllProducts() {
        log.info("Retrieving all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    public Mono<ProductResponse> getProduct(@PathVariable String productId) {
        log.info("Retrieving product: {}", productId);
        return productService.getProduct(productId);
    }

    @PutMapping("/{productId}/stock")
    public Mono<ProductResponse> updateStock(@PathVariable String productId,
                                             @RequestParam Integer quantity) {
        log.info("Updating stock for product: {} to quantity: {}", productId, quantity);
        return productService.updateStock(productId, quantity);
    }
}