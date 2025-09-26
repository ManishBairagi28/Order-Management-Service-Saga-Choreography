#!/bin/bash
echo "=== Cleaning and Setting Up Maven Project ==="

# Clean all target directories
echo "Cleaning previous builds..."
find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true

# Verify Maven version
echo "Maven version:"
mvn --version

# Verify project structure
echo "Verifying project structure..."
if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: Root pom.xml not found!"
    exit 1
fi

if [ ! -f "shared-events/pom.xml" ]; then
    echo "❌ ERROR: shared-events/pom.xml not found!"
    exit 1
fi

if [ ! -f "order-service/pom.xml" ]; then
    echo "❌ ERROR: order-service/pom.xml not found!"
    exit 1
fi

if [ ! -f "payment-service/pom.xml" ]; then
    echo "❌ ERROR: payment-service/pom.xml not found!"
    exit 1
fi

if [ ! -f "inventory-service/pom.xml" ]; then
    echo "❌ ERROR: inventory-service/pom.xml not found!"
    exit 1
fi

echo "✅ All pom.xml files found in correct locations"

# Verify Maven can parse the POMs
echo "Validating POM files..."
mvn validate

if [ $? -eq 0 ]; then
    echo "✅ All POM files are valid"
else
    echo "❌ POM validation failed"
    exit 1
fi

echo "Project structure validated successfully!"