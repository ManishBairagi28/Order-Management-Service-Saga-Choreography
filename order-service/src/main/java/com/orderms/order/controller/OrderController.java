package com.orderms.order.controller;

import com.orderms.order.dto.CreateOrderRequest;
import com.orderms.order.dto.OrderResponse;
import com.orderms.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        return orderService.createOrder(request)
                .doOnSuccess(response ->
                        log.info("Order created successfully: {}", response.getId()));
    }

    @GetMapping("/{orderId}")
    public Mono<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("Retrieving order: {}", orderId);
        return orderService.getOrder(orderId);
    }

    @GetMapping("/customer/{customerId}")
    public Flux<OrderResponse> getOrdersByCustomer(@PathVariable String customerId) {
        log.info("Retrieving orders for customer: {}", customerId);
        return orderService.getOrdersByCustomer(customerId);
    }
}