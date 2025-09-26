package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentProcessedEvent extends BaseEvent {
    private String paymentId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentProcessedEvent(String orderId, String sagaId, String paymentId,
                                 BigDecimal amount, String paymentMethod) {
        super(orderId, sagaId, "PAYMENT_PROCESSED");
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}