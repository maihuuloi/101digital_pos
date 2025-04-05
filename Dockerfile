FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR
COPY build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
