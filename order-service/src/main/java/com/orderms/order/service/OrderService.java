package com.orderms.order.service;

import com.orderms.order.dto.CreateOrderRequest;
import com.orderms.order.dto.OrderResponse;
import com.orderms.order.entity.Order;
import com.orderms.order.entity.SagaInstance;
import com.orderms.order.repository.OrderRepository;
import com.orderms.order.repository.SagaRepository;
import com.orderms.shared.events.OrderCreatedEvent;
import com.orderms.shared.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SagaRepository sagaRepository;
    private final EventPublisher eventPublisher;

    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;

    public Mono<OrderResponse> createOrder(CreateOrderRequest request) {
        String sagaId = UUID.randomUUID().toString();

        return Mono.fromCallable(() -> new Order(request.getCustomerId(), request.getOrderItems(), sagaId))
                .flatMap(orderRepository::save)
                .flatMap(order -> createSagaInstance(sagaId, order.getId())
                        .thenReturn(order))
                .flatMap(order -> publishOrderCreatedEvent(order)
                        .thenReturn(order))
                .map(this::toOrderResponse)
                .doOnSuccess(response ->
                        log.info("Order created successfully: {} with saga: {}",
                                response.getId(), sagaId))
                .doOnError(error ->
                        log.error("Failed to create order with saga: {}", sagaId, error));
    }

    private Mono<SagaInstance> createSagaInstance(String sagaId, String orderId) {
        SagaInstance sagaInstance = new SagaInstance(sagaId, orderId);
        return sagaRepository.save(sagaInstance);
    }

    private Mono<Void> publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getSagaId(),
                order.getCustomerId(),
                order.getOrderItems(),
                order.getTotalAmount()
        );

        return eventPublisher.publishEvent(orderEventsTopic, event);
    }

    public Mono<OrderResponse> getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::toOrderResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderId)));
    }

    public Flux<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId)
                .map(this::toOrderResponse);
    }

    public Mono<OrderResponse> updateOrderStatus(String orderId, OrderStatus status) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderId)))
                .flatMap(order -> {
                    order.updateStatus(status);
                    return orderRepository.save(order);
                })
                .map(this::toOrderResponse);
    }

    public Mono<OrderResponse> markOrderFailed(String orderId, String reason) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderId)))
                .flatMap(order -> {
                    order.setFailure(reason);
                    return orderRepository.save(order);
                })
                .map(this::toOrderResponse);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .orderItems(order.getOrderItems())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .sagaId(order.getSagaId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .failureReason(order.getFailureReason())
                .build();
    }
}