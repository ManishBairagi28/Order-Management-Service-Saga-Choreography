package com.orderms.inventory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product(String name, String description, BigDecimal price, Integer availableQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canReserve(Integer quantity) {
        return availableQuantity >= quantity;
    }

    public void reserveQuantity(Integer quantity) {
        if (canReserve(quantity)) {
            this.availableQuantity -= quantity;
            this.reservedQuantity += quantity;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Insufficient inventory to reserve");
        }
    }

    public void releaseReservedQuantity(Integer quantity) {
        if (reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.availableQuantity += quantity;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void confirmReservation(Integer quantity) {
        if (reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.updatedAt = LocalDateTime.now();
        }
    }
}