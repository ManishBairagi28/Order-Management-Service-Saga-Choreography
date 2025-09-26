package com.orderms.inventory.repository;

import com.orderms.inventory.entity.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Flux<Product> findByNameContaining(String name);

    Flux<Product> findByAvailableQuantityGreaterThan(Integer quantity);
}