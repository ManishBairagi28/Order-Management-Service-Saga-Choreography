package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentFailedEvent extends BaseEvent {
    private String reason;

    public PaymentFailedEvent(String orderId, String sagaId, String reason) {
        super(orderId, sagaId, "PAYMENT_FAILED");
        this.reason = reason;
    }
}