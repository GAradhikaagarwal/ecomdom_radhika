# Multi-stage Dockerfile for building and running the Spring Boot application

### Build stage
FROM maven:3.10.1-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy project files and perform a Maven package (skip tests for faster builds)
COPY . /workspace
RUN mvn -B -DskipTests package

### Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage. Uses a wildcard to match the Spring Boot jar.
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

# Allow runtime JVM options via JAVA_OPTS env var
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
