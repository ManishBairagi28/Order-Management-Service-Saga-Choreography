package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderCreatedEvent extends BaseEvent {
    private String customerId;
    private List<OrderItem> orderItems;
    private BigDecimal totalAmount;

    public OrderCreatedEvent(String orderId, String sagaId, String customerId,
                             List<OrderItem> orderItems, BigDecimal totalAmount) {
        super(orderId, sagaId, "ORDER_CREATED");
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.totalAmount = totalAmount;
    }
}