# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:24-jdk-slim

# Install dependencies required by Chrome & ChromeDriver
RUN apt-get update && apt-get install -y \
    wget unzip curl gnupg2 \
    libglib2.0-0 libnss3 libgconf-2-4 libxi6 \
    libxrandr2 libxss1 libxcursor1 libasound2 libatk1.0-0 \
    libpangocairo-1.0-0 libxcomposite1 libxdamage1 libxfixes3 \
    libatk-bridge2.0-0 libgtk-3-0 libgbm1 fonts-liberation \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome stable
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable

# Install matching ChromeDriver
RUN CHROME_VERSION=$(google-chrome --version | grep -oE '[0-9]+' | head -1) && \
    DRIVER_VERSION=$(curl -s "https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${CHROME_VERSION}") && \
    wget -q https://storage.googleapis.com/chrome-for-testing-public/${DRIVER_VERSION}/linux64/chromedriver-linux64.zip && \
    unzip chromedriver-linux64.zip && \
    mv chromedriver-linux64/chromedriver /usr/local/bin/ && \
    rm -rf chromedriver-linux64 chromedriver-linux64.zip

# Set working directory
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Environment variable for headless display
ENV DISPLAY=:99

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=80"]
