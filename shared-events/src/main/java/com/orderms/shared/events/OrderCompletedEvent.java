package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderCompletedEvent extends BaseEvent {
    public OrderCompletedEvent(String orderId, String sagaId) {
        super(orderId, sagaId, "ORDER_COMPLETED");
    }
}