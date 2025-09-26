package com.orderms.inventory.entity;

import com.orderms.shared.events.ReservedItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "inventory_reservations")
public class InventoryReservation {
    @Id
    private String id;
    private String orderId;
    private List<ReservedItem> reservedItems;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;

    public InventoryReservation(String orderId, List<ReservedItem> reservedItems) {
        this.orderId = orderId;
        this.reservedItems = reservedItems;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsConfirmed() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsReleased() {
        this.status = ReservationStatus.RELEASED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = ReservationStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public enum ReservationStatus {
        RESERVED, CONFIRMED, RELEASED, FAILED
    }
}