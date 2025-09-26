package com.orderms.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventListener implements ApplicationRunner {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-events}")
    private String paymentEventsTopic;

    @Value("${kafka.topics.inventory-events}")
    private String inventoryEventsTopic;

    @Override
    public void run(ApplicationArguments args) {
        kafkaReceiver.receive()
                .doOnNext(this::handleRecord)
                .doOnError(error -> log.error("Error consuming message", error))
                .retry()
                .subscribe();

        log.info("Order service event listener started");
    }

    private void handleRecord(ReceiverRecord<String, String> record) {
        try {
            String eventJson = record.value();
            String topic = record.topic();

            log.info("Received event from topic: {} with key: {}", topic, record.key());

            if (paymentEventsTopic.equals(topic)) {
                handlePaymentEvent(eventJson);
            } else if (inventoryEventsTopic.equals(topic)) {
                handleInventoryEvent(eventJson);
            } else {
                log.warn("Unknown topic: {}", topic);
            }

            record.receiverOffset().acknowledge();

        } catch (Exception e) {
            log.error("Error processing event: {}", record.value(), e);
        }
    }

    private void handlePaymentEvent(String eventJson) {
        try {
            // Determine event type first
            com.orderms.shared.events.BaseEvent baseEvent =
                    objectMapper.readValue(eventJson, com.orderms.shared.events.BaseEvent.class);

            switch (baseEvent.getEventType()) {
                case "PAYMENT_PROCESSED":
                    com.orderms.shared.events.PaymentProcessedEvent paymentProcessedEvent =
                            objectMapper.readValue(eventJson, com.orderms.shared.events.PaymentProcessedEvent.class);
                    sagaOrchestrator.handlePaymentProcessed(paymentProcessedEvent)
                            .subscribe(
                                    v -> log.info("Payment processed event handled for order: {}",
                                            paymentProcessedEvent.getOrderId()),
                                    error -> log.error("Failed to handle payment processed event", error)
                            );
                    break;

                case "PAYMENT_FAILED":
                    com.orderms.shared.events.PaymentFailedEvent paymentFailedEvent =
                            objectMapper.readValue(eventJson, com.orderms.shared.events.PaymentFailedEvent.class);
                    sagaOrchestrator.handlePaymentFailed(paymentFailedEvent)
                            .subscribe(
                                    v -> log.info("Payment failed event handled for order: {}",
                                            paymentFailedEvent.getOrderId()),
                                    error -> log.error("Failed to handle payment failed event", error)
                            );
                    break;

                default:
                    log.info("Ignoring payment event type: {}", baseEvent.getEventType());
            }

        } catch (Exception e) {
            log.error("Error handling payment event: {}", eventJson, e);
        }
    }

    private void handleInventoryEvent(String eventJson) {
        try {
            com.orderms.shared.events.BaseEvent baseEvent =
                    objectMapper.readValue(eventJson, com.orderms.shared.events.BaseEvent.class);

            switch (baseEvent.getEventType()) {
                case "INVENTORY_RESERVED":
                    com.orderms.shared.events.InventoryReservedEvent inventoryReservedEvent =
                            objectMapper.readValue(eventJson, com.orderms.shared.events.InventoryReservedEvent.class);
                    sagaOrchestrator.handleInventoryReserved(inventoryReservedEvent)
                            .subscribe(
                                    v -> log.info("Inventory reserved event handled for order: {}",
                                            inventoryReservedEvent.getOrderId()),
                                    error -> log.error("Failed to handle inventory reserved event", error)
                            );
                    break;

                case "INVENTORY_RESERVATION_FAILED":
                    com.orderms.shared.events.InventoryReservationFailedEvent inventoryFailedEvent =
                            objectMapper.readValue(eventJson, com.orderms.shared.events.InventoryReservationFailedEvent.class);
                    sagaOrchestrator.handleInventoryReservationFailed(inventoryFailedEvent)
                            .subscribe(
                                    v -> log.info("Inventory reservation failed event handled for order: {}",
                                            inventoryFailedEvent.getOrderId()),
                                    error -> log.error("Failed to handle inventory reservation failed event", error)
                            );
                    break;

                default:
                    log.info("Ignoring inventory event type: {}", baseEvent.getEventType());
            }

        } catch (Exception e) {
            log.error("Error handling inventory event: {}", eventJson, e);
        }
    }
}