package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderFailedEvent extends BaseEvent {
    private String reason;

    public OrderFailedEvent(String orderId, String sagaId, String reason) {
        super(orderId, sagaId, "ORDER_FAILED");
        this.reason = reason;
    }
}