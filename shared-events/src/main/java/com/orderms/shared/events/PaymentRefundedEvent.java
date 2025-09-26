package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentRefundedEvent extends BaseEvent {
    private String paymentId;
    private BigDecimal refundAmount;

    public PaymentRefundedEvent(String orderId, String sagaId, String paymentId,
                                BigDecimal refundAmount) {
        super(orderId, sagaId, "PAYMENT_REFUNDED");
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
    }
}