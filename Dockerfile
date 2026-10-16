# syntax=docker/dockerfile:1

# ---- Etapa 1: compilar el JAR con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -B -DskipTests package

# ---- Etapa 2: imagen final minima y sin root ----
# Alpine + JRE 21: superficie de ataque reducida. Se aplican los parches del sistema
# operativo disponibles al construir (Trivy exige 0 CVE HIGH/CRITICAL).
FROM eclipse-temurin:21-jre-alpine
RUN apk upgrade --no-cache && addgroup -S app && adduser -S app -G app
WORKDIR /app

RUN mkdir -p /app/data && chown -R app:app /app
COPY --from=build /app/target/atlas-api-1.0.0.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
