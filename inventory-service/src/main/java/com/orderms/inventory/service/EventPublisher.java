package com.orderms.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderms.shared.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;

    public Mono<Void> publishEvent(String topic, BaseEvent event) {
        return Mono.fromCallable(() -> {
                    try {
                        return objectMapper.writeValueAsString(event);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize event", e);
                    }
                })
                .flatMap(eventJson -> {
                    ProducerRecord<String, String> producerRecord =
                            new ProducerRecord<>(topic, event.getOrderId(), eventJson);

                    SenderRecord<String, String, String> senderRecord =
                            SenderRecord.create(producerRecord, event.getEventId());

                    return kafkaSender.send(Mono.just(senderRecord))
                            .next()
                            .doOnSuccess(result ->
                                    log.info("Event published successfully: {} to topic: {}",
                                            event.getEventType(), topic))
                            .doOnError(error ->
                                    log.error("Failed to publish event: {} to topic: {}",
                                            event.getEventType(), topic, error))
                            .then();
                })
                .onErrorResume(throwable -> {
                    log.error("Error publishing event to topic: {}", topic, throwable);
                    return Mono.error(throwable);
                });
    }
}