package com.orderms.inventory.service;

import com.orderms.inventory.dto.CreateProductRequest;
import com.orderms.inventory.dto.ProductResponse;
import com.orderms.inventory.entity.Product;
import com.orderms.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Mono<ProductResponse> createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getAvailableQuantity()
        );

        return productRepository.save(product)
                .map(this::toProductResponse)
                .doOnSuccess(response ->
                        log.info("Product created successfully: {}", response.getId()));
    }

    public Flux<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .map(this::toProductResponse);
    }

    public Mono<ProductResponse> getProduct(String productId) {
        return productRepository.findById(productId)
                .map(this::toProductResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)));
    }

    public Mono<ProductResponse> updateStock(String productId, Integer quantity) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)))
                .flatMap(product -> {
                    product.setAvailableQuantity(quantity);
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    return productRepository.save(product);
                })
                .map(this::toProductResponse);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQuantity(product.getAvailableQuantity())
                .reservedQuantity(product.getReservedQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}