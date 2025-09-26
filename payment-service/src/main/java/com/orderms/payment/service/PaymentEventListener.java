package com.orderms.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderms.shared.events.BaseEvent;
import com.orderms.shared.events.OrderCancelEvent;
import com.orderms.shared.events.OrderCreatedEvent;
import com.orderms.shared.events.OrderFailedEvent;
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
public class PaymentEventListener implements ApplicationRunner {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;

    @Override
    public void run(ApplicationArguments args) {
        kafkaReceiver.receive()
                .doOnNext(this::handleRecord)
                .doOnError(error -> log.error("Error consuming message", error))
                .onErrorContinue((error, obj) -> log.error("Error processing record: {}", obj, error))
                .retry()
                .subscribe();

        log.info("Payment service event listener started"); // ← CHECK FOR THIS MESSAGE
    }

    private void handleRecord(ReceiverRecord<String, String> record) {
        try {
            String eventJson = record.value();
            String topic = record.topic();

            log.info("Received event from topic: {} with key: {}", topic, record.key());
            log.debug("Event payload: {}", eventJson); // ← ADD THIS FOR DEBUGGING

            if (orderEventsTopic.equals(topic)) {
                handleOrderEvent(eventJson);
            } else {
                log.warn("Unknown topic: {}", topic);
            }

            record.receiverOffset().acknowledge();

        } catch (Exception e) {
            log.error("Error processing event: {}", record.value(), e);
            // Don't rethrow - acknowledge the record to avoid infinite retry
            record.receiverOffset().acknowledge();
        }
    }

    private void handleOrderEvent(String eventJson) {
        try {
            BaseEvent baseEvent = objectMapper.readValue(eventJson, BaseEvent.class);

            log.info("Processing event type: {}", baseEvent.getEventType());

            switch (baseEvent.getEventType()) {
                case "ORDER_CREATED":
                    log.info("Handling ORDER_CREATED event");
                    OrderCreatedEvent orderCreatedEvent =
                            objectMapper.readValue(eventJson, OrderCreatedEvent.class);
                    paymentService.processPayment(orderCreatedEvent)
                            .subscribe(
                                    v -> log.info("Order created event handled for order: {}",
                                            orderCreatedEvent.getOrderId()),
                                    error -> log.error("Failed to handle order created event", error)
                            );
                    break;

                case "ORDER_CANCEL":
                    log.info("Handling ORDER_CANCEL event");
                    OrderCancelEvent orderCancelEvent =
                            objectMapper.readValue(eventJson, OrderCancelEvent.class);
                    paymentService.refundPayment(orderCancelEvent)
                            .subscribe(
                                    v -> log.info("Order cancel event handled for order: {}",
                                            orderCancelEvent.getOrderId()),
                                    error -> log.error("Failed to handle order cancel event", error)
                            );
                    break;

                case "ORDER_FAILED":
                    log.info("Handling ORDER_FAILED event");
                    OrderFailedEvent orderFailedEvent =
                            objectMapper.readValue(eventJson, OrderFailedEvent.class);
                    // Handle order failed event if needed
                    log.info("Order failed event received for order: {}, reason: {}",
                            orderFailedEvent.getOrderId(), orderFailedEvent.getReason());
                    break;

                default:
                    log.info("Ignoring order event type: {}", baseEvent.getEventType());
            }

        } catch (Exception e) {
            log.error("Error handling order event: {}", eventJson, e);
        }
    }
}