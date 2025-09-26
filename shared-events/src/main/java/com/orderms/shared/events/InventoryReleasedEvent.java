package com.orderms.shared.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryReleasedEvent extends BaseEvent {
    private List<String> releasedItems;

    public InventoryReleasedEvent(String orderId, String sagaId, List<String> releasedItems) {
        super(orderId, sagaId, "INVENTORY_RELEASED");
        this.releasedItems = releasedItems;
    }
}