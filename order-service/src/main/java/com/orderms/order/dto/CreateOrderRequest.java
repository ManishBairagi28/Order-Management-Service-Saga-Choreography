package com.orderms.order.dto;

import com.orderms.shared.events.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order items cannot be empty")
    @NotNull(message = "Order items are required")
    private List<OrderItem> orderItems;
}