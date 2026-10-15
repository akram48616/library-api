# ───────── Stage 1: Build ─────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=10 \
    -Dmaven.wagon.http.pool=false \
    -Dhttp.keepAlive=false

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=10

# ───────── Stage 2: Run ─────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN mkdir -p /app/uploads/covers

COPY --from=build /app/target/library-api.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]