package com.orderms.order.service;

import com.orderms.order.entity.SagaInstance;
import com.orderms.order.repository.SagaRepository;
import com.orderms.shared.events.*;
import com.orderms.shared.model.OrderStatus;
import com.orderms.shared.saga.SagaState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final SagaRepository sagaRepository;
    private final OrderService orderService;
    private final EventPublisher eventPublisher;

    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;

    public Mono<Void> handlePaymentProcessed(PaymentProcessedEvent event) {
        return sagaRepository.findByOrderId(event.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Saga not found for order: " + event.getOrderId())))
                .flatMap(saga -> {
                    saga.markPaymentCompleted();
                    return sagaRepository.save(saga);
                })
                .flatMap(saga -> {
                    if (saga.canComplete()) {
                        return completeOrder(saga);
                    }
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Payment processed for order: {}", event.getOrderId()))
                .doOnError(error -> log.error("Failed to handle payment processed for order: {}",
                        event.getOrderId(), error));
    }

    public Mono<Void> handleInventoryReserved(InventoryReservedEvent event) {
        return sagaRepository.findByOrderId(event.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Saga not found for order: " + event.getOrderId())))
                .flatMap(saga -> {
                    saga.markInventoryReserved();
                    return sagaRepository.save(saga);
                })
                .flatMap(saga -> {
                    if (saga.canComplete()) {
                        return completeOrder(saga);
                    }
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Inventory reserved for order: {}", event.getOrderId()))
                .doOnError(error -> log.error("Failed to handle inventory reserved for order: {}",
                        event.getOrderId(), error));
    }

    public Mono<Void> handlePaymentFailed(PaymentFailedEvent event) {
        return sagaRepository.findByOrderId(event.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Saga not found for order: " + event.getOrderId())))
                .flatMap(saga -> {
                    saga.markPaymentFailed(event.getReason());
                    return sagaRepository.save(saga);
                })
                .flatMap(saga -> failOrder(saga, event.getReason()))
                .then()
                .doOnSuccess(v -> log.info("Payment failed for order: {}", event.getOrderId()))
                .doOnError(error -> log.error("Failed to handle payment failed for order: {}",
                        event.getOrderId(), error));
    }

    public Mono<Void> handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        return sagaRepository.findByOrderId(event.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Saga not found for order: " + event.getOrderId())))
                .flatMap(saga -> {
                    saga.markInventoryFailed(event.getReason());
                    return sagaRepository.save(saga);
                })
                .flatMap(saga -> {
                    // If payment was completed, we need to refund
                    if (saga.isPaymentCompleted()) {
                        return initiateCompensation(saga, event.getReason());
                    } else {
                        return failOrder(saga, event.getReason());
                    }
                })
                .then()
                .doOnSuccess(v -> log.info("Inventory reservation failed for order: {}", event.getOrderId()))
                .doOnError(error -> log.error("Failed to handle inventory reservation failed for order: {}",
                        event.getOrderId(), error));
    }

    private Mono<Void> completeOrder(SagaInstance saga) {
        return orderService.updateOrderStatus(saga.getOrderId(), OrderStatus.COMPLETED)
                .then(publishOrderCompletedEvent(saga))
                .then(updateSagaState(saga, SagaState.ORDER_COMPLETED));
    }

    private Mono<Void> failOrder(SagaInstance saga, String reason) {
        return orderService.markOrderFailed(saga.getOrderId(), reason)
                .then(publishOrderFailedEvent(saga, reason))
                .then(updateSagaState(saga, SagaState.ORDER_FAILED));
    }

    private Mono<Void> initiateCompensation(SagaInstance saga, String reason) {
        return publishOrderCancelEvent(saga, reason)
                .then(updateSagaState(saga, SagaState.COMPENSATING));
    }

    private Mono<Void> publishOrderCompletedEvent(SagaInstance saga) {
        OrderCompletedEvent event = new OrderCompletedEvent(saga.getOrderId(), saga.getSagaId());
        return eventPublisher.publishEvent(orderEventsTopic, event);
    }

    private Mono<Void> publishOrderFailedEvent(SagaInstance saga, String reason) {
        OrderFailedEvent event = new OrderFailedEvent(saga.getOrderId(), saga.getSagaId(), reason);
        return eventPublisher.publishEvent(orderEventsTopic, event);
    }

    private Mono<Void> publishOrderCancelEvent(SagaInstance saga, String reason) {
        OrderCancelEvent event = new OrderCancelEvent(saga.getOrderId(), saga.getSagaId(), reason);
        return eventPublisher.publishEvent(orderEventsTopic, event);
    }

    private Mono<Void> updateSagaState(SagaInstance saga, SagaState newState) {
        saga.updateState(newState);
        return sagaRepository.save(saga).then();
    }
}