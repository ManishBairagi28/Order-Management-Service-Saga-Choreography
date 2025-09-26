package com.orderms.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthController {

    private final ReactiveMongoTemplate mongoTemplate;

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        return mongoTemplate.getCollectionNames()
                .hasElements()
                .map(hasCollections -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "UP");
                    response.put("timestamp", LocalDateTime.now());
                    response.put("database", hasCollections ? "Connected" : "No collections");
                    response.put("service", "order-service");
                    return response;
                })
                .onErrorReturn(createErrorResponse());
    }

    private Map<String, Object> createErrorResponse() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "DOWN");
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("error", "Database connection failed");
        errorResponse.put("service", "order-service");
        return errorResponse;
    }
}