# Use official Playwright Java image (includes Chromium + dependencies)
FROM mcr.microsoft.com/playwright/java:v1.47.0-jammy

# Set working directory
WORKDIR /app

# Copy Maven/Gradle config first to cache dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download Maven dependencies
RUN ./mvnw dependency:go-offline

# Copy application code
COPY src ./src

# Package the Spring Boot app
RUN ./mvnw clean package -DskipTests

# Expose app port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "target/app.jar"]
