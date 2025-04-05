#!/bin/bash

set -e

# App settings
APP_NAME="process-order-service"
JAR_NAME="app.jar"
DOCKER_IMAGE_NAME="process-order-service:latest"

# Step 1: Clean & package with Maven
echo "🧱 Building the project with Maven..."
./mvnw clean package -DskipTests

# Step 2: Create the build/libs directory if it doesn't exist
mkdir -p build/libs

# Step 3: Copy the JAR to build context
echo "📦 Copying JAR to Docker context..."
cp target/*.jar build/libs/$JAR_NAME

# Step 4: Remove old Docker images
echo "🗑️ Removing old Docker images..."
docker rmi -f $(docker images -q $DOCKER_IMAGE_NAME) || true

# Step 5: Build the Docker image
echo "🐳 Building Docker image: $DOCKER_IMAGE_NAME..."
docker build -t $DOCKER_IMAGE_NAME .

#Step 6: Clean up old containers
docker-compose down
# Step 7: Run Docker Compose
echo "🚀 Starting services with Docker Compose..."
docker-compose up --build
