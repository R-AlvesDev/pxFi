# --- Stage 1: Build the application ---
# Use an official Maven image with Java 17 to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file and download dependencies
# This is done as a separate step to leverage Docker's layer caching.
# Dependencies will only be re-downloaded if pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping the tests for a faster build
RUN mvn package -DskipTests -Dcheckstyle.skip=true


# --- Stage 2: Run the application ---
# Use a lightweight JRE image for a smaller, more secure final container
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage
# The name of the JAR is taken from the pom.xml file
COPY --from=build /app/target/pxFi-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]