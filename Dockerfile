# Multi-stage Dockerfile for building and running the Spring Boot application

### Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy project files and perform a Maven package (skip tests for faster builds)
COPY . /workspace
# Build the project and then remove any .properties files from the produced JAR(s)
RUN chmod +x ./mvnw || true \
	&& ./mvnw -B -DskipTests package \
	&& apt-get update && apt-get install -y zip >/dev/null \
	&& for f in target/*.jar; do zip -d "$f" '*.properties' || true; done \
	&& rm -f target/classes/*.properties || true

### Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage. Uses a wildcard to match the Spring Boot jar.
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

# Allow runtime JVM options via JAVA_OPTS env var
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
