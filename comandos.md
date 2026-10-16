# Comandos — MVP Atlas (Caso A)

Todas las herramientas del pipeline aplicadas al MVP y en verde. Es la misma tabla del
proyecto base del curso, con los nombres de Atlas.

> Requisitos: Java 21, Maven 3.9+ (o `./mvnw`), Docker (SonarQube, Trivy y ZAP) y la
> CLI de Snyk (`npm i -g snyk`).

---

## Tabla: tecnología → comando → reporte

| # | Tecnología | Comando | Reporte / acceso |
| --- | --- | --- | --- |
| 01 | Spotless | `./mvnw spotless:check` | consola (sin archivo) |
| 02 | Checkstyle | `./mvnw checkstyle:check` | `target/checkstyle-result.xml` |
| 03 | PMD | `./mvnw pmd:check` | `target/site/pmd.html` · `target/pmd.xml` |
| 04 | SpotBugs | `./mvnw compile spotbugs:check` | `target/spotbugsXml.xml` |
| 05 | JUnit + Mockito | `./mvnw test` | `target/surefire-reports/` |
| 06 | PIT (mutación) | `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` |
| 07 | JaCoCo (cobertura) | `./mvnw verify` | `target/site/jacoco/index.html` |
| 08 | SonarQube | `docker compose up -d` + `./mvnw verify sonar:sonar -Dsonar.token=TU_TOKEN` | <http://localhost:9000> |
| 09 | Snyk | `snyk test --severity-threshold=critical` | consola (`--json` / `--sarif` opcional) |
| 10 | Cucumber (BDD) | `./mvnw test -Dtest=CucumberRunnerTest` | `target/cucumber-report.html` |
| 11 | Docker + Trivy | `docker build ...` + `trivy image ...` | `trivy-report.html` |
| 12 | OWASP ZAP | app levantada + `zap-baseline.py ...` | `zap-report.html` |
| 13 | Gatling | app levantada + `./mvnw gatling:test` | `target/gatling/<simulación>/index.html` |
| 14 | JMeter | app levantada + perfil `-Pjmeter` | `target/jmeter/results/*.csv` |
| — | Postman | Collection Runner con `postman/Atlas.postman_collection.json` | resumen del Runner |

---

## A) Calidad de código + pruebas + mutación + cobertura (no necesita la app)

```bash
./mvnw clean verify
```

Ejecuta en orden **Spotless → Checkstyle → JUnit/Cucumber → PMD → SpotBugs → JaCoCo → PIT**.

| Reporte | Archivo |
| --- | --- |
| Code smells (PMD) | `target/site/pmd.html` |
| Bugs (SpotBugs) | `target/spotbugsXml.xml` |
| Pruebas (JUnit + Cucumber) | `target/surefire-reports/` · `target/cucumber-report.html` |
| Mutación (PIT) | `target/pit-reports/index.html` |
| Cobertura (JaCoCo) | `target/site/jacoco/index.html` |

## B) Herramientas que necesitan la aplicación levantada (12, 13, 14)

Terminal 1:

```bash
./mvnw spring-boot:run
```

Terminal 2:

```bash
# 12 - OWASP ZAP (DAST, sin autenticación, igual que el pipeline)
docker run --rm --network host -v "$PWD":/zap/wrk/:rw \
  ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
  -t http://localhost:8080 -i -c .zap/rules.tsv -r zap-report.html

# 13 - Gatling (10 usuarios; -Dvu=N para cambiarlo)
./mvnw gatling:test

# 14 - JMeter (50 usuarios por defecto)
./mvnw -Pjmeter jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results

# 14 - JMeter con 200 usuarios (NFR-PERF-3, prueba del informe)
./mvnw -Pjmeter -Djmeter.usuarios=200 -Djmeter.rampa=60 \
  jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
```

## C) Contenedor (11 — Docker + Trivy)

```bash
docker build -t atlas-api:1.0.0 .

docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image --scanners vuln --pkg-types os \
  --severity CRITICAL,HIGH --exit-code 1 atlas-api:1.0.0

# Reporte HTML para el informe
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v "$PWD":/out \
  aquasec/trivy image --scanners vuln --pkg-types os \
  --format template --template "@/contrib/html.tpl" \
  --output /out/trivy-report.html atlas-api:1.0.0
```

## D) SonarQube (08)

```bash
docker compose up -d      # SonarQube en http://localhost:9000 (admin / admin)
./mvnw verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN
```

## E) Snyk (09)

```bash
snyk auth                 # una vez
snyk test --severity-threshold=critical
```

## F) Postman

1. Importar `postman/Atlas.postman_collection.json` y `postman/Atlas-local.postman_environment.json`.
2. Seleccionar el entorno **Atlas local**.
3. Botón **Run** en la colección → **Run Atlas MVP** (en orden, 1 iteración).

---

## Resumen de reportes

| Reporte | Archivo / URL |
| --- | --- |
| Code smells (PMD) | `target/site/pmd.html` |
| Convenciones (Checkstyle) | `target/checkstyle-result.xml` |
| Bugs (SpotBugs) | `target/spotbugsXml.xml` |
| Pruebas (JUnit / Cucumber) | `target/surefire-reports/` · `target/cucumber-report.html` |
| Mutación (PIT) | `target/pit-reports/index.html` |
| Cobertura (JaCoCo) | `target/site/jacoco/index.html` |
| Calidad global (SonarQube) | <http://localhost:9000> o SonarCloud |
| Vulnerabilidades de la imagen (Trivy) | `trivy-report.html` |
| DAST (ZAP) | `zap-report.html` |
| Carga (Gatling) | `target/gatling/<simulación>/index.html` |
| Carga (JMeter) | `target/jmeter/results/*.csv` |
