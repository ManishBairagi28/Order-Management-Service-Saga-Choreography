#!/bin/bash

echo "=== Building All Services ==="

# Build Order Service
echo "Building Order Service..."
cd order-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "Order Service built successfully"
else
    echo "Order Service build failed"
    exit 1
fi
cd ..

# Build Payment Service
echo "Building Payment Service..."
cd payment-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "Payment Service built successfully"
else
    echo "Payment Service build failed"
    exit 1
fi
cd ..

# Build Inventory Service
echo "Building Inventory Service..."
cd inventory-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "Inventory Service built successfully"
else
    echo "Inventory Service build failed"
    exit 1
fi
cd ..

echo "All services built successfully!"