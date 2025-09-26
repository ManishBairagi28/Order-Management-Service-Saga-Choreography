package com.orderms.payment.service;

import com.orderms.payment.entity.Payment;
import com.orderms.payment.repository.PaymentRepository;
import com.orderms.shared.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    @Value("${kafka.topics.payment-events}")
    private String paymentEventsTopic;

    public Mono<Void> processPayment(OrderCreatedEvent orderEvent) {
        log.info("Processing payment for order: {}", orderEvent.getOrderId());

        return createPayment(orderEvent)
                .flatMap(this::simulatePaymentProcessing)
                .flatMap(payment -> handlePaymentResult(payment, orderEvent.getSagaId()))
                .doOnSuccess(v -> log.info("Payment processing completed for order: {}", orderEvent.getOrderId()))
                .doOnError(error -> log.error("Payment processing failed for order: {}",
                        orderEvent.getOrderId(), error));
    }

    private Mono<Payment> createPayment(OrderCreatedEvent orderEvent) {
        Payment payment = new Payment(
                orderEvent.getOrderId(),
                orderEvent.getCustomerId(),
                orderEvent.getTotalAmount(),
                "CREDIT_CARD" // Default payment method
        );

        return paymentRepository.save(payment);
    }

    private Mono<Payment> simulatePaymentProcessing(Payment payment) {
        return Mono.fromCallable(() -> {
                    payment.setStatus(Payment.PaymentStatus.PROCESSING);
                    return payment;
                })
                .flatMap(paymentRepository::save)
                .delayElement(Duration.ofSeconds(2)) // Simulate processing time
                .flatMap(savedPayment -> Mono.fromCallable(() -> {
                    // Simulate payment processing logic
                    // In real scenario, this would integrate with payment gateway

                    // Simulate success/failure based on amount (for demo purposes)
                    boolean paymentSuccess = savedPayment.getAmount().compareTo(BigDecimal.valueOf(1000)) <= 0;

                    if (paymentSuccess) {
                        String transactionId = "TXN-" + UUID.randomUUID().toString();
                        savedPayment.markAsProcessed(transactionId);
                        log.info("Payment processed successfully for order: {} with transaction: {}",
                                savedPayment.getOrderId(), transactionId);
                    } else {
                        savedPayment.markAsFailed("Payment amount exceeds limit");
                        log.warn("Payment failed for order: {} - amount exceeds limit", savedPayment.getOrderId());
                    }

                    return savedPayment;
                }))
                .flatMap(paymentRepository::save);
    }

    private Mono<Void> handlePaymentResult(Payment payment, String sagaId) {
        if (payment.getStatus() == Payment.PaymentStatus.PROCESSED) {
            return publishPaymentProcessedEvent(payment, sagaId);
        } else {
            return publishPaymentFailedEvent(payment, sagaId);
        }
    }

    private Mono<Void> publishPaymentProcessedEvent(Payment payment, String sagaId) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getOrderId(),
                sagaId,
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentMethod()
        );

        return eventPublisher.publishEvent(paymentEventsTopic, event);
    }

    private Mono<Void> publishPaymentFailedEvent(Payment payment, String sagaId) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                payment.getOrderId(),
                sagaId,
                payment.getFailureReason()
        );

        return eventPublisher.publishEvent(paymentEventsTopic, event);
    }

    public Mono<Void> refundPayment(OrderCancelEvent cancelEvent) {
        log.info("Processing refund for order: {}", cancelEvent.getOrderId());

        return paymentRepository.findByOrderId(cancelEvent.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found for order: " + cancelEvent.getOrderId())))
                .flatMap(payment -> {
                    if (payment.getStatus() == Payment.PaymentStatus.PROCESSED) {
                        payment.markAsRefunded();
                        return paymentRepository.save(payment);
                    } else {
                        return Mono.just(payment);
                    }
                })
                .flatMap(payment -> publishPaymentRefundedEvent(payment, cancelEvent.getSagaId()))
                .doOnSuccess(v -> log.info("Refund processed for order: {}", cancelEvent.getOrderId()))
                .doOnError(error -> log.error("Failed to process refund for order: {}",
                        cancelEvent.getOrderId(), error));
    }

    private Mono<Void> publishPaymentRefundedEvent(Payment payment, String sagaId) {
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                payment.getOrderId(),
                sagaId,
                payment.getId(),
                payment.getAmount()
        );

        return eventPublisher.publishEvent(paymentEventsTopic, event);
    }
}