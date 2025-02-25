# Specify platform explicitly
FROM --platform=linux/amd64 eclipse-temurin:17-jre-jammy

WORKDIR /app

# Add a volume pointing to /tmp
VOLUME /tmp

# Copy the fat JAR file
COPY target/pet-clinic-api-1.0.0-fat.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# debug settings
ENV JAVA_OPTS="-Xmx512m -Xms256m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]