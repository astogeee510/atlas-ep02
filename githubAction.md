# Pipeline de GitHub Actions — MVP Atlas

El pipeline vive en `.github/workflows/calidad-codigo.yml` y recorre **las 14
tecnologías del curso**, cada una como un *job* encadenado con `needs`: si un job falla,
los siguientes no se ejecutan. Es el mismo pipeline del proyecto base (API de Alumnos)
aplicado a Atlas, con un paso extra por herramienta que publica el resultado en el
**resumen del job** (Summary) para capturarlo en el informe.

---

## 1. Flujo de jobs

```
spotless → checkstyle → pmd → spotbugs → test → mutation → jacoco
        → sonarqube → snyk → cucumber → container → dast
        → performance (Gatling) → performance-jmeter
```

| Orden | Job | Nombre en GitHub | Comando | Qué responde | Evidencia |
| --- | --- | --- | --- | --- | --- |
| 1 | `spotless` | Spotless (formato de código) | `mvn spotless:check` | ¿El código respeta el formato común? | log del job |
| 2 | `checkstyle` | Checkstyle (convenciones de código) | `mvn checkstyle:check` | ¿Respeta las convenciones de nombres e imports? | log del job |
| 3 | `pmd` | PMD (code smells) | `mvn pmd:check` | ¿Hay código muerto o malas prácticas? | artefacto `pmd-report` |
| 4 | `spotbugs` | SpotBugs (bugs en el bytecode) | `mvn compile spotbugs:check` | ¿Hay bugs potenciales en el bytecode? | artefacto `spotbugs-report` |
| 5 | `test` | Pruebas unitarias (JUnit + Mockito) | `mvn test` | ¿Las reglas y la seguridad funcionan? | Summary + `surefire-reports` |
| 6 | `mutation` | Mutación (PIT) | `mvn test-compile org.pitest:pitest-maven:mutationCoverage` | ¿Las pruebas detectan cambios en la lógica? (≥ 80 %) | Summary + `pit-report` |
| 7 | `jacoco` | JaCoCo (cobertura de pruebas) | `mvn test jacoco:report jacoco:check` | ¿Qué parte del código ejecutan las pruebas? (≥ 80 %) | Summary + `jacoco-report` |
| 8 | `sonarqube` | SonarQube (calidad) | `mvn test jacoco:report sonar:sonar` | ¿Pasa el Quality Gate? | SonarCloud |
| 9 | `snyk` | Snyk (seguridad de dependencias) | `snyk test --severity-threshold=critical` | ¿Hay dependencias con CVE críticos? | log del job |
| 10 | `cucumber` | Cucumber (BDD / integración) | `mvn test -Dtest=CucumberRunnerTest` | ¿Se cumplen los escenarios de negocio? | Summary + `cucumber-report` |
| 11 | `container` | Contenedor (Docker + Trivy) | `docker build` + Trivy | ¿La imagen tiene CVE HIGH/CRITICAL en el SO? | Summary + `trivy-report` |
| 12 | `dast` | DAST (OWASP ZAP) | app + `zaproxy/action-baseline` | ¿La app en ejecución expone fallas de cabeceras? | Summary + `zap_scan` |
| 13 | `performance` | Rendimiento (Gatling) | app + `mvn gatling:test` | ¿p95 < 300 ms y 0 errores con 10 usuarios? | Summary + `gatling-report` |
| 14 | `performance-jmeter` | Rendimiento (JMeter) | app + perfil `-Pjmeter` | ¿0 % de errores con 50 usuarios? | Summary + `jmeter-results` |

### Disparadores

- `push` a `main`, cualquier `pull_request` y `workflow_dispatch` (botón **Run workflow**).
- `concurrency` cancela ejecuciones anteriores del mismo ref; `permissions: contents: read`.

---

## 2. Secretos de SonarQube y Snyk

El pipeline **funciona sin secretos**: SonarQube y Snyk terminan en verde con el aviso
*"pendiente"* en el resumen. Para activarlos:

> Repo → **Settings** → **Secrets and variables** → **Actions**

| Tipo | Nombre | Valor |
| --- | --- | --- |
| Secret | `SONAR_TOKEN` | Token de SonarCloud (My Account → Security → Generate Tokens) |
| Secret | `SONAR_HOST_URL` | `https://sonarcloud.io` |
| Variable | `SONAR_ORGANIZATION` | La *organization key* de SonarCloud |
| Secret | `SNYK_TOKEN` | app.snyk.io → Account settings → Auth Token |

### SonarCloud paso a paso

1. Entrar a <https://sonarcloud.io> con la cuenta de GitHub.
2. **+ → Analyze new project** → elegir `atlas-ep02` (instalar la app de SonarCloud en el repo si lo pide).
3. En el proyecto: **Administration → Analysis Method** → desactivar **Automatic Analysis**
   (el análisis lo hace el pipeline).
4. Revisar que el *project key* sea `atlas-api`; si SonarCloud asigna otro (por ejemplo
   `astogeee510_atlas-ep02`), cambiar `sonar.projectKey` en `sonar-project.properties`.
5. **My Account → Security → Generate Tokens** → copiar el token.
6. Crear los secretos y la variable de la tabla.
7. **Actions → Calidad de código → Run workflow**.

### Snyk paso a paso

1. Entrar a <https://app.snyk.io> con la cuenta de GitHub.
2. **Account settings → Auth Token → Click to show** → copiar.
3. Crear el secreto `SNYK_TOKEN` y volver a ejecutar el workflow.

> Los tokens **nunca** van en el código ni en el repositorio: siempre como secretos.

---

## 3. Cómo ver los resultados

1. Pestaña **Actions** → workflow **Calidad de código** → última ejecución.
2. El grafo muestra los 14 jobs en cadena (captura para el informe).
3. Debajo del grafo, cada job publica su resumen: pruebas, mutación, cobertura, Trivy,
   ZAP, Gatling y JMeter (capturas para la sección de ejecución del informe).
4. Los reportes completos se descargan en **Artifacts** al final de la página.

---

## 4. Cómo probar el pipeline localmente

```bash
./mvnw clean verify                    # jobs 1 a 7 en un comando
./mvnw spotless:check
./mvnw checkstyle:check
./mvnw pmd:check
./mvnw compile spotbugs:check
./mvnw test
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
./mvnw test jacoco:report jacoco:check
./mvnw test -Dtest=CucumberRunnerTest
```

Con la app levantada (`./mvnw spring-boot:run`) en otra terminal:

```bash
./mvnw gatling:test
./mvnw -Pjmeter jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
```

---

## 5. Notas

- **SonarQube no se levanta en CI**: `docker-compose.yml` es para uso local. En CI se usa SonarCloud.
- **Trivy** corta el pipeline solo con CVE HIGH/CRITICAL del sistema operativo (igual que el
  proyecto base); el reporte completo con librerías queda en el resumen como información.
- **ZAP** corre con `fail_action: true`: si `SecurityHeadersFilter` deja de enviar las
  cabeceras, el job falla.
- Los jobs que levantan la app guardan `app.log` y lo suben como artefacto si fallan.
