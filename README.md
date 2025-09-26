# Order Management System with Saga Pattern

A comprehensive microservices implementation using **Java Reactive Programming**, **Saga Design Pattern**, **Event-Driven Architecture**, **Apache Kafka**, and **MongoDB**.

## Architecture Overview

This system demonstrates enterprise-grade microservices with distributed transaction management using:

- **Spring Boot 3.x** with **Spring WebFlux** for reactive programming
- **Project Reactor** (Mono/Flux) for non-blocking operations
- **MongoDB Reactive** for data persistence
- **Apache Kafka** with **Reactor Kafka** for event-driven messaging
- **Choreography-based Saga Pattern** for distributed transactions

## Services

### 1. Order Service (Port 8080) - Saga Orchestrator
- Manages order lifecycle
- Orchestrates distributed transactions
- Handles saga state management
- REST API endpoints for order operations

### 2. Payment Service (Port 8081) - Saga Participant
- Processes payments
- Simulates payment gateway integration
- Handles payment failures and refunds
- Event-driven processing

### 3. Inventory Service (Port 8082) - Saga Participant
- Manages product inventory
- Handles stock reservations
- Supports inventory release and confirmation
- Product management APIs

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

# Order Management System — Saga, Kafka, Reactive Java, MongoDB

This project implements an **Order Management System** using:

* **Saga Pattern (Choreography)** for distributed transactions
* **Event-Driven Architecture** with Kafka
* **Reactive Java (Spring WebFlux + Reactor)**
* **Reactive MongoDB** for persistence

---

## 🚀 Getting Started

### 1. Start Infrastructure

```bash
chmod +x setup.sh
./setup.sh
```

### 2. Build Services

```bash
chmod +x build-all.sh
./build-all.sh
```

### 3. Start Services

```bash
chmod +x start-services.sh
./start-services.sh
```

### 4. Test System

```bash
chmod +x test-system.sh
./test-system.sh
```

---

## 📌 API Endpoints

### Order Service

* `POST /api/v1/orders` → Create order
* `GET /api/v1/orders/{id}` → Get order by ID
* `GET /api/v1/orders/customer/{customerId}` → Get customer orders

### Inventory Service

* `POST /api/v1/products` → Create product
* `GET /api/v1/products` → Get all products
* `GET /api/v1/products/{id}` → Get product by ID
* `PUT /api/v1/products/{id}/stock` → Update stock

---

## 💻 Example Usage

### Create Product

```bash
curl -X POST http://localhost:8082/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming Laptop",
    "price": 999.99,
    "availableQuantity": 10
  }'
```

### Create Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "orderItems": [
      {
        "productId": "PRODUCT_ID",
        "productName": "Laptop",
        "quantity": 1,
        "unitPrice": 999.99,
        "totalPrice": 999.99
      }
    ]
  }'
```

---

## 📊 Monitoring

* **Kafka UI**: [http://localhost:8084](http://localhost:8084)
* **Mongo Express**: [http://localhost:8083](http://localhost:8083) (credentials: `admin/admin`)
* **Service logs**: available in `*.log` files

---

## 🛑 Stopping Services

```bash
chmod +x stop-services.sh
./stop-services.sh

docker-compose down
```

---

## 🔍 Testing Scenarios

1. **Successful Order**: Payment succeeds → Inventory reserved → Order completed
2. **Payment Failure**: Payment fails (amount > $1000) → Order marked as failed
3. **Inventory Shortage**: Insufficient stock → Compensation & refund triggered

---

## 🏗️ Architecture Patterns

* **Saga Pattern**: Choreography-based distributed transaction management
* **Event-Driven Architecture**: Loose coupling through Kafka events
* **CQRS**: Separate read/write models with reactive repositories
* **Reactive Programming**: Non-blocking I/O throughout the stack
* **Database per Service**: Independent data management
* **Circuit Breaker**: Failure isolation and recovery
* **Event Sourcing**: Complete audit trail of events

---

## ✨ Key Features

* **Distributed Transactions** with Saga pattern
* **Compensating Transactions** for automatic rollback
* **Event-Driven Communication** via Kafka
* **Reactive Streams** for high-performance non-blocking ops
* **Idempotent Operations** with safe retry mechanisms
* **Monitoring & Observability**: event tracking, logs, Kafka UI
* **Scalable Architecture**: independent service scaling

---

## 📂 File Structure

```
order-management-system/
├── docker-compose.yml
├── setup.sh
├── build-all.sh
├── start-services.sh
├── stop-services.sh
├── test-system.sh
├── README.md
├── shared-events/
│   └── [shared event classes]
├── order-service/
│   ├── src/main/java/com/orderms/order/
│   ├── src/main/resources/application.yml
│   └── pom.xml
├── payment-service/
│   ├── src/main/java/com/orderms/payment/
│   ├── src/main/resources/application.yml
│   └── pom.xml
└── inventory-service/
    ├── src/main/java/com/orderms/inventory/
    ├── src/main/resources/application.yml
    └── pom.xml
```

---

## 📜 License

This project is licensed under the **MIT License**.
