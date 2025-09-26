package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryReservationFailedEvent extends BaseEvent {
    private String reason;
    private List<String> unavailableItems;

    public InventoryReservationFailedEvent(String orderId, String sagaId,
                                           String reason, List<String> unavailableItems) {
        super(orderId, sagaId, "INVENTORY_RESERVATION_FAILED");
        this.reason = reason;
        this.unavailableItems = unavailableItems;
    }
}