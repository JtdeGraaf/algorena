# ---- Runtime Stage ----
FROM eclipse-temurin:25-alpine
WORKDIR /app

# Copy the pre-built JAR file
COPY target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
