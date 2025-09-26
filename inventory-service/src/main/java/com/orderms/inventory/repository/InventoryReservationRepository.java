package com.orderms.inventory.repository;

import com.orderms.inventory.entity.InventoryReservation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryReservationRepository extends ReactiveMongoRepository<InventoryReservation, String> {

    Mono<InventoryReservation> findByOrderId(String orderId);
}