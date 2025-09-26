#!/bin/bash

echo "=== Testing Order Management System ==="

# Base URLs
ORDER_SERVICE="http://localhost:8080"
INVENTORY_SERVICE="http://localhost:8082"

# Function to create product
create_product() {
    echo "Creating test product..."
    PRODUCT_RESPONSE=$(curl -s -X POST ${INVENTORY_SERVICE}/api/v1/products \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Laptop",
            "description": "Gaming Laptop for Testing",
            "price": 999.99,
            "availableQuantity": 10
        }')

    PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "Product created with ID: $PRODUCT_ID"
}

# Function to test successful order
test_successful_order() {
    echo "Testing successful order flow..."
    ORDER_RESPONSE=$(curl -s -X POST ${ORDER_SERVICE}/api/v1/orders \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"test-customer-1\",
            \"orderItems\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"productName\": \"Test Laptop\",
                    \"quantity\": 1,
                    \"unitPrice\": 999.99,
                    \"totalPrice\": 999.99
                }
            ]
        }")

    ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "Order created with ID: $ORDER_ID"

    # Wait for saga completion
    echo "Waiting for saga completion..."
    sleep 10

    # Check final order status
    FINAL_STATUS=$(curl -s ${ORDER_SERVICE}/api/v1/orders/${ORDER_ID} | grep -o '"status":"[^"]*' | cut -d'"' -f4)
    echo "Final order status: $FINAL_STATUS"

    if [ "$FINAL_STATUS" = "COMPLETED" ]; then
        echo "✅ Successful order test PASSED"
    else
        echo "❌ Successful order test FAILED"
    fi
}

# Function to test payment failure
test_payment_failure() {
    echo "Testing payment failure scenario..."
    ORDER_RESPONSE=$(curl -s -X POST ${ORDER_SERVICE}/api/v1/orders \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"test-customer-2\",
            \"orderItems\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"productName\": \"Expensive Item\",
                    \"quantity\": 1,
                    \"unitPrice\": 1500.00,
                    \"totalPrice\": 1500.00
                }
            ]
        }")

    ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "Order created with ID: $ORDER_ID"

    # Wait for saga completion
    echo "Waiting for saga completion..."
    sleep 10

    # Check final order status
    FINAL_STATUS=$(curl -s ${ORDER_SERVICE}/api/v1/orders/${ORDER_ID} | grep -o '"status":"[^"]*' | cut -d'"' -f4)
    echo "Final order status: $FINAL_STATUS"

    if [ "$FINAL_STATUS" = "FAILED" ]; then
        echo "✅ Payment failure test PASSED"
    else
        echo "❌ Payment failure test FAILED"
    fi
}

# Function to test inventory shortage
test_inventory_shortage() {
    echo "Testing inventory shortage scenario..."
    ORDER_RESPONSE=$(curl -s -X POST ${ORDER_SERVICE}/api/v1/orders \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": \"test-customer-3\",
            \"orderItems\": [
                {
                    \"productId\": \"$PRODUCT_ID\",
                    \"productName\": \"Test Laptop\",
                    \"quantity\": 20,
                    \"unitPrice\": 50.00,
                    \"totalPrice\": 1000.00
                }
            ]
        }")

    ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "Order created with ID: $ORDER_ID"

    # Wait for saga completion
    echo "Waiting for saga completion..."
    sleep 10

    # Check final order status
    FINAL_STATUS=$(curl -s ${ORDER_SERVICE}/api/v1/orders/${ORDER_ID} | grep -o '"status":"[^"]*' | cut -d'"' -f4)
    echo "Final order status: $FINAL_STATUS"

    if [ "$FINAL_STATUS" = "FAILED" ]; then
        echo "✅ Inventory shortage test PASSED"
    else
        echo "❌ Inventory shortage test FAILED"
    fi
}

# Run tests
create_product
test_successful_order
test_payment_failure
test_inventory_shortage

echo "System testing completed!"