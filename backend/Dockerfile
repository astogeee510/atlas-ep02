# syntax=docker/dockerfile:1

# ---- Etapa 1: compilar el JAR con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline -DskipTests || true
COPY . .
RUN mvn -q -B -DskipTests -Dspotless.check.skip=true -Dcheckstyle.skip=true package

# ---- Etapa 2: imagen final minima y sin root ----
FROM eclipse-temurin:21-jre-alpine
# apk upgrade: aplica los parches de seguridad de Alpine publicados despues de la imagen base
RUN apk upgrade --no-cache && addgroup -S app && adduser -S app -G app
WORKDIR /app
RUN mkdir -p /app/data/storage && chown -R app:app /app
COPY --from=build /app/target/atlas-api-1.0.0.jar app.jar
USER app

ENV ATLAS_STORAGE_DIR=/app/data/storage
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
