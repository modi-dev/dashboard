# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom and resolve deps first (better layer caching)
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
COPY src/main/resources/binaries ./src/main/resources/binaries
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests clean package


# Runtime stage (Linux, JRE 17)
FROM eclipse-temurin:17-jre-jammy

ENV TZ=UTC \
    JAVA_OPTS="" \
    MANAGEMENT_PORT=8080 \
    SERVER_PORT=3001

WORKDIR /opt/app

# Copy the fat jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose application and actuator ports
EXPOSE 3001 8080

# Run as non-root user for better security
RUN useradd -r -u 1001 appuser && chown -R appuser:appuser /opt/app
USER appuser

# Healthcheck (optional): checks liveness endpoint if enabled
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:${MANAGEMENT_PORT}/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -Dmanagement.server.port=${MANAGEMENT_PORT} -jar app.jar"]


