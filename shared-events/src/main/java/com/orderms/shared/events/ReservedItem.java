package com.orderms.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservedItem {
    private String productId;
    private Integer reservedQuantity;
    private String reservationId;
}