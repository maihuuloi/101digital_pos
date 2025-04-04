#!/bin/bash

APP_NAME="pos"
JAR_FILE="target/${APP_NAME}-0.0.1-SNAPSHOT.jar"
IMAGE_NAME="pos-service"

echo "🔨 Step 1: Building Spring Boot App..."
./mvnw clean package -DskipTests

if [ ! -f "$JAR_FILE" ]; then
  echo "❌ Build failed: JAR file not found!"
  exit 1
fi

echo "🐘 Step 2: Starting PostgreSQL with Docker Compose..."
docker-compose up -d

echo "⏳ Waiting for PostgreSQL to start..."
sleep 10

echo "🐳 Step 3: Building Docker image for the app..."
docker build -t $IMAGE_NAME .

echo "🚀 Step 4: Running app container..."
docker run --rm \
  --name pos-service \
  --network pos_default \  # join Docker Compose network
  -p 8080:8080 \
  $IMAGE_NAME
