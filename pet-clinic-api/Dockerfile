FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Add a volume pointing to /tmp
VOLUME /tmp

# Copy the JAR file
COPY target/pet-clinic-api-1.0.0.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]