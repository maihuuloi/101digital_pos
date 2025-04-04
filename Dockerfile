# Use official OpenJDK base image
FROM eclipse-temurin:17-jdk-alpine

# Set app directory
WORKDIR /app

# Copy JAR file
COPY target/coffeeshop-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Tomcat)
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
