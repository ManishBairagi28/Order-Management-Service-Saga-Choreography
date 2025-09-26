package com.orderms.order.entity;

import com.orderms.shared.saga.SagaState;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Document(collection = "saga_instances")
public class SagaInstance {
    @Id
    private String sagaId;
    private String orderId;
    private SagaState currentState;
    private Set<String> completedSteps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean paymentCompleted;
    private boolean inventoryReserved;
    private String compensationReason;

    public SagaInstance(String sagaId, String orderId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.currentState = SagaState.STARTED;
        this.completedSteps = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentCompleted = false;
        this.inventoryReserved = false;
    }

    public void updateState(SagaState newState) {
        this.currentState = newState;
        this.updatedAt = LocalDateTime.now();
    }

    public void addCompletedStep(String step) {
        this.completedSteps.add(step);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canComplete() {
        return paymentCompleted && inventoryReserved;
    }

    public void markPaymentCompleted() {
        this.paymentCompleted = true;
        addCompletedStep("PAYMENT");
        updateState(SagaState.PAYMENT_COMPLETED);
    }

    public void markInventoryReserved() {
        this.inventoryReserved = true;
        addCompletedStep("INVENTORY");
        updateState(SagaState.INVENTORY_RESERVED);
    }

    public void markPaymentFailed(String reason) {
        updateState(SagaState.PAYMENT_FAILED);
        this.compensationReason = reason;
    }

    public void markInventoryFailed(String reason) {
        updateState(SagaState.INVENTORY_RESERVATION_FAILED);
        this.compensationReason = reason;
    }
}