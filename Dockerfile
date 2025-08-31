# Use an official JDK base image
FROM openjdk:17-jdk-slim

# Install dependencies required for Chromium (Playwright)
RUN apt-get update && apt-get install -y \
    wget gnupg ca-certificates fonts-liberation \
    libappindicator3-1 libasound2 libatk-bridge2.0-0 libatk1.0-0 \
    libcups2 libdbus-1-3 libgdk-pixbuf2.0-0 libnspr4 libnss3 \
    libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 \
    xdg-utils chromium chromium-driver && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper & POM first to leverage Docker caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/app.jar"]
