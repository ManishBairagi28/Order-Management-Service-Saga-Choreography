
package com.orderms.shared.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "ORDER_CREATED"),
    @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "ORDER_COMPLETED"),
    @JsonSubTypes.Type(value = OrderCancelEvent.class, name = "ORDER_CANCEL"),
    @JsonSubTypes.Type(value = OrderFailedEvent.class, name = "ORDER_FAILED"),
    @JsonSubTypes.Type(value = PaymentProcessedEvent.class, name = "PAYMENT_PROCESSED"),
    @JsonSubTypes.Type(value = PaymentFailedEvent.class, name = "PAYMENT_FAILED"),
    @JsonSubTypes.Type(value = PaymentRefundedEvent.class, name = "PAYMENT_REFUNDED"),
    @JsonSubTypes.Type(value = InventoryReservedEvent.class, name = "INVENTORY_RESERVED"),
    @JsonSubTypes.Type(value = InventoryReservationFailedEvent.class, name = "INVENTORY_RESERVATION_FAILED"),
    @JsonSubTypes.Type(value = InventoryReleasedEvent.class, name = "INVENTORY_RELEASED")
})
public abstract class BaseEvent {
    private String eventId = UUID.randomUUID().toString();
    private String orderId;
    private String sagaId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private String eventType;

    public BaseEvent(String orderId, String sagaId, String eventType) {
        this.orderId = orderId;
        this.sagaId = sagaId;
        this.eventType = eventType;
    }
}
