#!/bin/bash

echo "=== Order Management System Setup ==="

# Check prerequisites
echo "Checking prerequisites..."
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
maven_version=$(mvn -version 2>&1 | grep "Apache Maven" | awk '{print $3}')
docker_version=$(docker --version 2>&1 | awk '{print $3}' | sed 's/,//')

echo "Java version: $java_version"
echo "Maven version: $maven_version"
echo "Docker version: $docker_version"

# Start infrastructure
echo "Starting infrastructure with Docker Compose..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 30

# Create Kafka topics
echo "Creating Kafka topics..."
docker exec -it kafka kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec -it kafka kafka-topics --create --topic payment-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec -it kafka kafka-topics --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# List created topics
echo "Verifying topics..."
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

echo "Infrastructure setup completed!"
echo "Available services:"
echo "- Kafka: localhost:9092"
echo "- MongoDB: localhost:27017"
echo "- Kafka UI: http://localhost:8084"
echo "- Mongo Express: http://localhost:8083 (admin/admin)"