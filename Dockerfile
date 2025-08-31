# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Runtime stage
FROM mcr.microsoft.com/playwright/java:v1.47.0-jammy
WORKDIR /app
RUN adduser --disabled-password --gecos '' appuser && \
    mkdir -p /home/appuser && chown -R appuser:appuser /home/appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
