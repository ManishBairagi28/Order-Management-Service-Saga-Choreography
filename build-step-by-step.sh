#!/bin/bash
echo "=== Step by Step Build Process ==="

# Step 1: Clean everything
echo "Step 1: Cleaning all modules..."
mvn clean
if [ $? -ne 0 ]; then
    echo "❌ Clean failed"
    exit 1
fi
echo "✅ Clean successful"

# Step 2: Install shared-events first
echo "Step 2: Installing shared-events module..."
cd shared-events
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ shared-events install failed"
    exit 1
fi
cd ..
echo "✅ shared-events installed successfully"

# Step 3: Build all services
echo "Step 3: Building all services..."
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi
echo "✅ Compilation successful"

# Step 4: Package all services
echo "Step 4: Packaging all services..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Packaging failed"
    exit 1
fi
echo "✅ Packaging successful"

echo "🎉 Build completed successfully!"
echo "JAR files created:"
find . -name "*.jar" -path "*/target/*" | grep -v "original-"