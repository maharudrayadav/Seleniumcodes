# Use official OpenJDK image
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Install Playwright dependencies + browsers
RUN apt-get update && apt-get install -y \
    wget gnupg ca-certificates fonts-liberation libappindicator3-1 libasound2 \
    libatk-bridge2.0-0 libatk1.0-0 libcups2 libdbus-1-3 libgdk-pixbuf2.0-0 \
    libnspr4 libnss3 libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 \
    xdg-utils unzip && rm -rf /var/lib/apt/lists/*

# Build Spring Boot project
RUN mvn clean package -DskipTests

# Final runtime image
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Install dependencies again for Chromium runtime
RUN apt-get update && apt-get install -y \
    wget gnupg ca-certificates fonts-liberation libappindicator3-1 libasound2 \
    libatk-bridge2.0-0 libatk1.0-0 libcups2 libdbus-1-3 libgdk-pixbuf2.0-0 \
    libnspr4 libnss3 libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 \
    xdg-utils && rm -rf /var/lib/apt/lists/*

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Install Playwright browsers
RUN java -cp app.jar com.microsoft.playwright.CLI install chromium

# Expose Spring Boot default port
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java","-jar","/app/app.jar"]
