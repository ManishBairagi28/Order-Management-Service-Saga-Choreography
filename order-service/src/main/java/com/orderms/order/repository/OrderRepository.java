package com.orderms.order.repository;

import com.orderms.order.entity.Order;
import com.orderms.shared.model.OrderStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    Flux<Order> findByCustomerId(String customerId);

    Mono<Order> findBySagaId(String sagaId);

    Flux<Order> findByStatus(OrderStatus status);

    Flux<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
}