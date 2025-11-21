# Simple Dockerfile for local development
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src/ ./src/
COPY schema.sql .

# Build the JAR
RUN mvn clean package -DskipTests

# Create data directory for SQLite
RUN mkdir -p /app/data

# Expose socket server port
EXPOSE 8080

# Run the JAR in server mode
CMD ["java", "-jar", "target/poker-server.jar", "--server"]
