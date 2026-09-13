# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first and download deps separately -> this layer stays cached
# across rebuilds as long as pom.xml doesn't change, so `mvn package` on every
# code change doesn't re-download the internet.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Runs as non-root for basic container hardening.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM container-awareness flags: respect the memory limit Docker/K8s gives the
# container instead of reading host memory (default JVM behavior pre-container-flags
# can cause OOM kills under cgroup limits).
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
