package com.orderms.order.dto;

import com.orderms.shared.events.OrderItem;
import com.orderms.shared.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String customerId;
    private List<OrderItem> orderItems;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String sagaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;
}