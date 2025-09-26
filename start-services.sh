#!/bin/bash

echo "=== Starting All Microservices ==="

# Function to start service in background
start_service() {
    local service_name=$1
    local port=$2

    echo "Starting $service_name on port $port..."
    cd $service_name
    mvn spring-boot:run > ../${service_name}.log 2>&1 &
    local pid=$!
    echo $pid > ../${service_name}.pid
    echo "$service_name started with PID $pid"
    cd ..

    # Wait a bit before starting next service
    sleep 10
}

# Start services
start_service "order-service" "8080"
start_service "payment-service" "8081"
start_service "inventory-service" "8082"

echo "All services started!"
echo "Service logs are available in:"
echo "- order-service.log"
echo "- payment-service.log"
echo "- inventory-service.log"

echo "Service endpoints:"
echo "- Order Service: http://localhost:8080"
echo "- Payment Service: http://localhost:8081"
echo "- Inventory Service: http://localhost:8082"