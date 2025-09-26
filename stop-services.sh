#!/bin/bash

echo "=== Stopping All Microservices ==="

# Function to stop service
stop_service() {
    local service_name=$1

    if [ -f "${service_name}.pid" ]; then
        local pid=$(cat ${service_name}.pid)
        echo "Stopping $service_name (PID: $pid)..."
        kill $pid
        rm ${service_name}.pid
        echo "$service_name stopped"
    else
        echo "$service_name PID file not found"
    fi
}

# Stop services
stop_service "order-service"
stop_service "payment-service"
stop_service "inventory-service"

echo "All services stopped!"