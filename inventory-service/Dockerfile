# Inventory Service Dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Build application
RUN ./mvnw package -DskipTests

# Expose port
EXPOSE 8081

# Run application
CMD ["java", "-jar", "target/inventory-service-0.0.1-SNAPSHOT.jar"]