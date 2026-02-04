# Backend Dockerfile
FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Expose port
EXPOSE 8080

# Run the application
CMD ["./mvnw", "spring-boot:run"]
