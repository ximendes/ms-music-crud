# Use a lightweight OpenJDK base image
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your target (Maven) or build/libs (Gradle) folder
# Replace 'your-app.jar' with your actual filename
COPY ./build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]