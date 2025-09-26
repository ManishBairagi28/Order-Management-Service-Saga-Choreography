package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderCancelEvent extends BaseEvent {
    private String reason;

    public OrderCancelEvent(String orderId, String sagaId, String reason) {
        super(orderId, sagaId, "ORDER_CANCEL");
        this.reason = reason;
    }
}