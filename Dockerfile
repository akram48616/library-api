# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first — Docker layer caching
# if pom.xml unchanged → skip dependency download
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR — skip tests (tests run in CI/CD)
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Run
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads/covers

# Copy JAR from build stage
# uses finalName from pom.xml → library-api.jar
COPY --from=build /app/target/library-api.jar app.jar

# Expose port
EXPOSE 8080

# Health check — Railway/Render uses this
HEALTHCHECK \
    --interval=30s \
    --timeout=10s \
    --start-period=60s \
    --retries=3 \
    CMD wget -qO- \
        http://localhost:8080/actuator/health \
        || exit 1

# Run Spring Boot
# -Dspring.profiles.active=prod → uses application-prod.properties
# -XX:+UseContainerSupport      → respects Docker memory limits
# -XX:MaxRAMPercentage=75.0     → uses max 75% of container RAM
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]