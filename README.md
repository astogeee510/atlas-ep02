# Atlas · Gestión de clientes, contratos y documentos

![Pipeline](https://github.com/USUARIO/REPOSITORIO/actions/workflows/pipeline.yml/badge.svg)

Plataforma web de **CreaLab SpA** para que las pymes chilenas gestionen sus clientes, contratos y documentos,
construida según el **ERS Caso A – Sistema de Gestión Atlas v1.1** y auditada con el pipeline completo de
calidad y seguridad del curso **ISY1102 – Seguridad y Calidad en el Desarrollo de Software (EP02)**.

| | |
| --- | --- |
| **Integrantes** | `[Nombre completo 1]` · `[Nombre completo 2]` · `[Nombre completo 3]` |
| **Sección / Docente** | `[Sección]` · `[Docente]` |
| **Informe EP02** | [`docs/Informe_EP02_CasoA_Atlas.docx`](docs/Informe_EP02_CasoA_Atlas.docx) |
| **Evidencias** | [`evidencias/`](evidencias/) · [`docs/resultados-herramientas/`](docs/resultados-herramientas/) |

---

## 1. Stack y desviación justificada

| Capa | Tecnología | ERS |
| --- | --- | --- |
| Frontend | React 19.1.1 · MUI 7.3.4 · Tailwind 3.4.14 · axios 1.20.0 · Vite | §4.1 (axios actualizado desde 1.13.1 por CVE, ver §8) |
| Backend | **Spring Boot 3.3.5 · Java 21 · Spring Security · JWT (jjwt) · Flyway** | §2.4 pide Express → **desviación** |
| Base de datos | PostgreSQL 16 | §2.1 |
| Infraestructura | Docker Compose · nginx con TLS | NFR-SEG-1 |

**Desviación:** el ERS indica Express para el backend. Se usa Spring Boot porque el pipeline del curso
(Spotless, Checkstyle, PMD, SpotBugs, JUnit, PIT, JaCoCo, SonarQube, Snyk, Cucumber, Docker, Trivy, ZAP,
Gatling y JMeter) está definido sobre `pom.xml` (proyecto base `clase-main`). El **contrato REST** del ERS se
mantiene: rutas `/api/...`, JSON, JWT de corta vigencia, bcrypt y control de acceso por rol en el backend.

## 2. Arquitectura

```text
Navegador ──HTTPS──> nginx (TLS, cabeceras de seguridad, SPA React)
                       └── /api ──HTTP interno──> Spring Boot (JWT, RBAC, AES-GCM) ──> PostgreSQL
                                                         └── storage interno de documentos (volumen)
```

```text
backend/src/main/java/cl/crealab/atlas
├── domain          # entidades, reglas (EstadoContrato, PoliticaContrasena, ValidadorRut, DetectorTipoDocumento)
├── application     # casos de uso (Auth, Empresa, UsuarioEmpresa, Cliente, Contrato, Documento, Auditoria, ControlAcceso)
└── infrastructure  # REST, seguridad (JWT, filtros, limitador), cifrado AES-GCM, storage, persistencia JPA
frontend/src        # pages (8 del ERS §3.1.1), components, api (axios), auth (contexto de sesión)
```

## 3. Cómo levantarlo (ambiente QA)

Requisitos: Docker Desktop (o Docker Engine + Compose v2) y Git Bash/WSL en Windows.

```bash
bash scripts/generar-env.sh                              # crea infra/.env con secretos aleatorios
docker compose -f infra/docker-compose.yml up -d --build
bash scripts/esperar-atlas.sh                            # espera a que responda "UP"
docker compose -f infra/docker-compose.yml ps            # evidencia Anexo 0.1
```

| Recurso | URL |
| --- | --- |
| Aplicación | https://localhost (certificado autofirmado de QA: aceptar la advertencia) |
| Swagger UI | https://localhost/swagger-ui.html |
| API directa (Postman/JMeter/Gatling) | http://localhost:8080 (solo 127.0.0.1) |

**Usuarios de prueba (datos sintéticos)** — contraseña `Atlas#Demo2026`:

| Usuario | Empresa | Rol |
| --- | --- | --- |
| `admin.demo` | 1 · CreaLab Demo SpA | ADMIN |
| `editor.demo` | 1 · CreaLab Demo SpA | EDITOR |
| `admin.otra` | 2 · Otra Empresa Ltda | ADMIN |

Desarrollo sin Docker: `cd backend && ./mvnw spring-boot:run` (PostgreSQL local `atlas/atlas`) y
`cd frontend && npm ci && npm run dev` (http://localhost:5173, con proxy a `/api`).

## 4. Endpoints

| Método | Ruta | Rol | RF |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | anónimo | RF-2.1 |
| POST | `/api/auth/login` · `/api/auth/refresh` | anónimo | RF-1.1 |
| POST | `/api/auth/logout` · GET `/api/auth/me` | autenticado | RF-1.2 |
| POST/GET | `/api/empresas` · GET `/api/empresas/{id}/dashboard` | autenticado / miembro | RF-2.2, RF-2.3 |
| GET/POST/PUT/DELETE | `/api/empresas/{id}/usuarios[/{usuarioId}]` | ADMIN | RF-3.1, RF-3.3 |
| GET | `/api/empresas/{id}/clientes?nombre=&pagina=&tamano=` | miembro | RF-4.2 |
| POST | `/api/empresas/{id}/clientes` · PUT/DELETE `/api/clientes/{id}` | ADMIN | RF-4.1, RF-4.2 |
| GET | `/api/clientes/{id}/contratos` · `/api/contratos/{id}` | miembro | RF-5.1, RF-5.4 |
| POST/PUT/DELETE | `/api/contratos[/{id}]` | ADMIN | RF-5.1 |
| POST | `/api/contratos/{id}/documentos` (multipart `archivo`) | ADMIN | RF-5.2, RF-5.3 |
| GET | `/api/contratos/{id}/documentos[/descarga]` | miembro | RF-5.2 |
| POST/DELETE | `/api/contratos/{id}/documentos/enlace` · GET `/api/compartido/{token}` | ADMIN / público con token | RF-5.5 |
| GET | `/api/empresas/{id}/auditoria` | ADMIN | RF-6.1 |

## 5. Controles de seguridad (NFR-SEG)

| NFR | Implementación | Caso |
| --- | --- | --- |
| SEG-1 TLS | nginx TLS 1.2/1.3, redirección 80→443, HSTS | CP-SEG-03 |
| SEG-2 bcrypt | `BCryptPasswordEncoder(12)` + política de 12 caracteres | CP-SEG-03 |
| SEG-3 Cifrado en reposo | `CampoCifradoConverter` AES-256-GCM (contacto y datos bancarios) | CP-SEG-03 |
| SEG-4 RBAC backend | `ControlAcceso` por empresa y rol en cada caso de uso | CP-SEG-01 |
| SEG-5 CSRF/XSS/SQLi | JPA parametrizado, Bean Validation con listas blancas, escape de React, CSP, cookie `SameSite=Strict`, validación de archivos por firma | CP-SEG-02, CP-SEG-05 |
| SEG-6 Logs | sin contraseñas ni tokens, errores 500 genéricos, auditoría separada | CP-SEG-04 |
| SEG-7 Respaldo | `scripts/backup.sh` · `scripts/restore.sh` | CP-SEG-04 |
| SEG-8 Ley 19.628/21.719 | consentimiento en el registro, página de privacidad, enmascaramiento para EDITOR | CP-SEG-04 |
| SEG-9 JWT | acceso 15 min + refresco 7 días (cookie HttpOnly) + revocación por `jti` | CP-FUN-02 |

## 6. Pipeline (GitHub Actions)

Archivo: [`.github/workflows/pipeline.yml`](.github/workflows/pipeline.yml). Si un job falla, los siguientes no corren.

| # | Job | Herramienta | Comando local | Reporte |
| --- | --- | --- | --- | --- |
| 01 | spotless | Spotless | `cd backend && ./mvnw spotless:check` | consola |
| 02 | checkstyle | Checkstyle | `./mvnw checkstyle:check` | `target/checkstyle-result.xml` |
| 03 | pmd | PMD | `./mvnw pmd:check` | `target/site/pmd.html` |
| 04 | spotbugs | SpotBugs | `./mvnw compile spotbugs:check` | `target/spotbugsXml.xml` |
| 05 | test | JUnit 5 + Mockito + Testcontainers | `./mvnw test` | `target/surefire-reports/` |
| 06 | mutation | PIT (umbral 60 %) | `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` |
| 07 | jacoco | JaCoCo (umbral 70 % líneas) | `./mvnw test jacoco:report jacoco:check` | `target/site/jacoco/index.html` |
| 08 | sonarqube | SonarQube / SonarCloud | `./mvnw test jacoco:report sonar:sonar -Dsonar.token=...` | dashboard |
| 09 | snyk | Snyk | `snyk test --all-projects --severity-threshold=critical` | consola |
| 10 | cucumber | Cucumber BDD | `./mvnw test -Dtest=CucumberRunnerTest` | `target/cucumber-report.html` |
| 11 | fe-lint | ESLint (react, jsx-a11y, security) | `cd frontend && npm run lint` | `eslint-report.json` |
| 12 | fe-test | Vitest | `npm run test:coverage` | `coverage/` |
| 13 | fe-audit | npm audit | `npm audit --omit=dev --audit-level=high` | `npm-audit.json` |
| 14 | fe-build | Vite | `npm run build` | `dist/` |
| 15 | container | Docker + Trivy | `docker build ...` + `trivy image ...` | consola |
| 16 | e2e | Cypress | `npx cypress run` (con el stack levantado) | capturas |
| 17 | dast | OWASP ZAP baseline | ver §7 | `report_html` |
| 18 | lighthouse | Lighthouse CI | `npx @lhci/cli autorun` | `lhci-report/` |
| 19 | performance | Gatling | `./mvnw gatling:test` (API levantada) | `target/gatling/` |
| 20 | performance-jmeter | JMeter | `./mvnw -Pjmeter verify` | `target/jmeter/results/` |

**Secretos** (Settings → Secrets and variables → Actions): `SONAR_TOKEN`, `SONAR_HOST_URL`
(`https://sonarcloud.io`), `SONAR_ORGANIZATION` (solo SonarCloud) y `SNYK_TOKEN`. Sin ellos, esos dos jobs se
omiten con advertencia y el resto del pipeline funciona.

## 7. Ejecución manual de las herramientas (para las capturas)

```bash
# Postman: importar postman/Atlas.postman_collection.json y postman/QA.postman_environment.json,
# desactivar "SSL certificate verification" y fijar el working directory en la raíz del repo. O con newman:
npx newman run postman/Atlas.postman_collection.json -e postman/QA.postman_environment.json --insecure --working-dir .

# OWASP ZAP (Docker)
docker run --rm --network host -v "$PWD":/zap/wrk/:rw ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py -t https://localhost -c backend/.zap/rules.tsv -i -r zap-report.html

# JMeter 200 usuarios (GUI: abrir backend/src/test/jmeter/atlas.jmx)
cd backend && ./mvnw -Pjmeter -Djmeter.usuarios=200 verify

# Lighthouse: Chrome → F12 → Lighthouse → Mobile → Performance + Accessibility

# Capturas automáticas de la interfaz con fecha y ejecutor
cd scripts/capturas && npm run instalar && EJECUTOR="Nombre Apellido" npm run capturar

# Respaldo, restauración y disponibilidad
bash scripts/backup.sh && bash scripts/restore.sh backups/<archivo>.sql.gz
bash scripts/uptime.sh 60 10
```

## 8. Casos de prueba y trazabilidad

22 casos (`CP-FUN-01..06`, `CP-INT-01..04`, `CP-SEG-01..05`, `CP-SAST-01`, `CP-PERF-01/02`, `CP-USA-01`,
`CP-COMPAT-01`, `CP-REG-01`, `CP-DIS-01`) con cobertura del 100 % del ERS: 17/17 RF, 9/9 NFR-SEG, 3/3 NFR-PERF,
4/4 NFR-USAB, 2/2 NFR-DIS, 2/2 NFR-COMPAT y 5/5 RB. El detalle y la matriz están en el informe (sección 4).

**Hallazgos de análisis estático ya obtenidos en desarrollo** (archivos en `docs/resultados-herramientas/`):

| Herramienta | Resultado inicial | Acción | Resultado final |
| --- | --- | --- | --- |
| npm audit | 12 vulnerabilidades (2 críticas en vitest/tinypool, 6 altas: axios 1.13.1 y cadena de Tailwind 3) | axios → 1.20.0, vitest → 5.0.5 | 0 en producción; 7 en dependencias de build (Tailwind 3, riesgo aceptado) |
| ESLint | 1 error `jsx-a11y/no-autofocus` | eliminado `autoFocus` | 0 errores, 0 advertencias |
| Checkstyle / Spotless / PMD (curso) | 0 | — | 0 |
| PMD quickstart (revisión ampliada) | 5 avisos de baja prioridad | documentados en el informe | — |
| Lighthouse (login, móvil) | Performance 92 · Accesibilidad 100 · Buenas prácticas 96 · LCP 1,9 s | — | — |

## 9. Evidencias

| Anexo | Caso | Archivo |
| --- | --- | --- |
| 0.1 | Ambiente | `evidencias/Anexo_0.1_ambiente_docker-compose-ps.png` |
| 1.1 – 1.6 | CP-FUN-01..06 | `evidencias/Anexo_1.x_CP-FUN-0x_*.png` |
| 2.1 – 2.4 | CP-INT-01..04 | `evidencias/Anexo_2.x_CP-INT-0x_*.png` |
| 3.1 – 3.6 | CP-SEG-01..05, CP-SAST-01 | `evidencias/Anexo_3.x_*.png` |
| 4.1 – 4.2 | CP-PERF-01/02 | `evidencias/Anexo_4.x_*.png` |
| 5.1 – 5.3 | CP-USA-01, CP-COMPAT-01, CP-REG-01 | `evidencias/Anexo_5.x_*.png` |
| 6.1 | CP-DIS-01 | `evidencias/Anexo_6.1_CP-DIS-01_uptime-flyway.png` |
| 7.1 – 7.6 | Pipeline y reportes | `evidencias/Anexo_7.x_*.png` |

La lista completa con lo que debe mostrar cada captura está en [`evidencias/LEEME.md`](evidencias/LEEME.md).

## 10. Respaldo, restauración y migraciones

- Migraciones versionadas con Flyway en `backend/src/main/resources/db/migration` (NFR-DIS-2).
- Datos sintéticos de QA en `db/seed` (el perfil `prod` no los carga).
- Respaldo y restauración con `scripts/backup.sh` y `scripts/restore.sh` (NFR-SEG-7).
