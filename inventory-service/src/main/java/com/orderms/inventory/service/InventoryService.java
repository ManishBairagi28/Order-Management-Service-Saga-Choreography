package com.orderms.inventory.service;

import com.orderms.inventory.entity.InventoryReservation;
import com.orderms.inventory.entity.Product;
import com.orderms.inventory.repository.InventoryReservationRepository;
import com.orderms.inventory.repository.ProductRepository;
import com.orderms.shared.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;

    @Value("${kafka.topics.inventory-events}")
    private String inventoryEventsTopic;

    public Mono<Void> reserveInventory(OrderCreatedEvent orderEvent) {
        log.info("Reserving inventory for order: {}", orderEvent.getOrderId());

        return validateAndReserveItems(orderEvent.getOrderItems())
                .flatMap(reservedItems -> createReservation(orderEvent.getOrderId(), reservedItems))
                .flatMap(reservation -> publishInventoryReservedEvent(reservation, orderEvent.getSagaId()))
                .doOnSuccess(v -> log.info("Inventory reserved successfully for order: {}", orderEvent.getOrderId()))
                .onErrorResume(error -> {
                    log.error("Failed to reserve inventory for order: {}", orderEvent.getOrderId(), error);
                    return publishInventoryReservationFailedEvent(orderEvent, error.getMessage());
                });
    }

    private Mono<List<ReservedItem>> validateAndReserveItems(List<OrderItem> orderItems) {
        List<ReservedItem> reservedItems = new ArrayList<>();

        return Flux.fromIterable(orderItems)
                .flatMap(orderItem -> reserveProductQuantity(orderItem))
                .collectList()
                .map(items -> {
                    reservedItems.addAll(items);
                    return reservedItems;
                });
    }

    private Mono<ReservedItem> reserveProductQuantity(OrderItem orderItem) {
        return productRepository.findById(orderItem.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + orderItem.getProductId())))
                .flatMap(product -> {
                    if (product.canReserve(orderItem.getQuantity())) {
                        product.reserveQuantity(orderItem.getQuantity());
                        return productRepository.save(product)
                                .map(savedProduct -> new ReservedItem(
                                        product.getId(),
                                        orderItem.getQuantity(),
                                        UUID.randomUUID().toString()
                                ));
                    } else {
                        return Mono.error(new RuntimeException(
                                "Insufficient inventory for product: " + orderItem.getProductName()));
                    }
                });
    }

    private Mono<InventoryReservation> createReservation(String orderId, List<ReservedItem> reservedItems) {
        InventoryReservation reservation = new InventoryReservation(orderId, reservedItems);
        return reservationRepository.save(reservation);
    }

    private Mono<Void> publishInventoryReservedEvent(InventoryReservation reservation, String sagaId) {
        InventoryReservedEvent event = new InventoryReservedEvent(
                reservation.getOrderId(),
                sagaId,
                reservation.getReservedItems()
        );

        return eventPublisher.publishEvent(inventoryEventsTopic, event);
    }

    private Mono<Void> publishInventoryReservationFailedEvent(OrderCreatedEvent orderEvent, String reason) {
        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(
                orderEvent.getOrderId(),
                orderEvent.getSagaId(),
                reason,
                List.of() // Could include unavailable items
        );

        return eventPublisher.publishEvent(inventoryEventsTopic, event);
    }

    public Mono<Void> releaseInventory(OrderCancelEvent cancelEvent) {
        log.info("Releasing inventory for order: {}", cancelEvent.getOrderId());

        return reservationRepository.findByOrderId(cancelEvent.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Reservation not found for order: " + cancelEvent.getOrderId())))
                .flatMap(reservation -> {
                    return Flux.fromIterable(reservation.getReservedItems())
                            .flatMap(this::releaseReservedProduct)
                            .then(Mono.defer(() -> {
                                reservation.markAsReleased();
                                return reservationRepository.save(reservation);
                            }));
                })
                .flatMap(reservation -> publishInventoryReleasedEvent(reservation, cancelEvent.getSagaId()))
                .doOnSuccess(v -> log.info("Inventory released for order: {}", cancelEvent.getOrderId()))
                .doOnError(error -> log.error("Failed to release inventory for order: {}",
                        cancelEvent.getOrderId(), error));
    }

    private Mono<Product> releaseReservedProduct(ReservedItem reservedItem) {
        return productRepository.findById(reservedItem.getProductId())
                .flatMap(product -> {
                    product.releaseReservedQuantity(reservedItem.getReservedQuantity());
                    return productRepository.save(product);
                });
    }

    private Mono<Void> publishInventoryReleasedEvent(InventoryReservation reservation, String sagaId) {
        List<String> releasedItems = reservation.getReservedItems().stream()
                .map(ReservedItem::getProductId)
                .toList();

        InventoryReleasedEvent event = new InventoryReleasedEvent(
                reservation.getOrderId(),
                sagaId,
                releasedItems
        );

        return eventPublisher.publishEvent(inventoryEventsTopic, event);
    }

    public Mono<Void> confirmReservation(OrderCompletedEvent completedEvent) {
        log.info("Confirming inventory reservation for completed order: {}", completedEvent.getOrderId());

        return reservationRepository.findByOrderId(completedEvent.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Reservation not found for order: " + completedEvent.getOrderId())))
                .flatMap(reservation -> {
                    return Flux.fromIterable(reservation.getReservedItems())
                            .flatMap(this::confirmReservedProduct)
                            .then(Mono.defer(() -> {
                                reservation.markAsConfirmed();
                                return reservationRepository.save(reservation);
                            }));
                })
                .then()
                .doOnSuccess(v -> log.info("Inventory reservation confirmed for order: {}", completedEvent.getOrderId()))
                .doOnError(error -> log.error("Failed to confirm inventory reservation for order: {}",
                        completedEvent.getOrderId(), error));
    }

    private Mono<Product> confirmReservedProduct(ReservedItem reservedItem) {
        return productRepository.findById(reservedItem.getProductId())
                .flatMap(product -> {
                    product.confirmReservation(reservedItem.getReservedQuantity());
                    return productRepository.save(product);
                });
    }
}