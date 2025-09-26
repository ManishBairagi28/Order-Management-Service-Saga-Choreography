package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryReservedEvent extends BaseEvent {
    private List<ReservedItem> reservedItems;

    public InventoryReservedEvent(String orderId, String sagaId, List<ReservedItem> reservedItems) {
        super(orderId, sagaId, "INVENTORY_RESERVED");
        this.reservedItems = reservedItems;
    }
}