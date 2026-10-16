# API Atlas — MVP Caso A (EP02 · ISY1102)

MVP de **Atlas**, la plataforma de CreaLab SpA descrita en el ERS del **Caso A**:
gestión de **clientes** y de sus **contratos** con estado calculado, **login con JWT**
y **roles ADMIN / EDITOR**. Sigue la misma estructura y el mismo pipeline del
proyecto base del curso (API de Alumnos): **Spring Boot 3.5 + Maven + SQLite**,
arquitectura limpia y 14 herramientas de calidad y seguridad en GitHub Actions.

Incluye:

- Login con JWT, logout con revocación del token y bloqueo tras 5 intentos fallidos.
- Roles: **ADMIN** (lectura y escritura) y **EDITOR** (solo lectura).
- CRUD de clientes con validación de RUT, filtro por nombre, paginación y
  **eliminación lógica con confirmación**.
- Contratos por cliente con **estado calculado** (ACTIVO, POR_VENCER, EXPIRADO).
- Log de auditoría de acciones críticas, sin datos personales.
- **OpenAPI** con contrato YAML propio (contract-first), **Swagger UI**, **Redoc** y **Home**.
- Pipeline `calidad-codigo.yml` con los **14 jobs** del curso.

> La versión anterior del proyecto (backend completo + frontend React, 20 jobs) quedó
> en la rama `v1-fullstack`. Este MVP la reemplaza en `main` para ajustarse al
> alcance de la EP02 y al repositorio de referencia del curso.

---

## Alcance del MVP frente al ERS

| Requisito del ERS | En el MVP | Cómo |
| --- | --- | --- |
| RF-1.1 Login con usuario y contraseña | ✅ | `POST /api/v1/auth/login` → JWT |
| RF-1.2 Cierre de sesión y revocación | ✅ | `POST /api/v1/auth/logout` revoca el `jti` |
| RF-3.2 / RB-4 Editor solo lectura | ✅ | Spring Security: GET para ADMIN/EDITOR, escritura solo ADMIN |
| RB-5 Administrador con acceso completo | ✅ | Rol ADMIN |
| RF-4.1 Crear clientes | ✅ | `POST /api/v1/clientes` |
| RF-4.2 Editar, eliminar, listar con paginación y filtro | ✅ | Eliminación lógica con `confirmar=true` |
| RF-4.3 Validaciones y formato | ✅ | RUT módulo 11, correo, teléfono, sin `<` `>` |
| RF-5.1 Contratos asociados al cliente | ✅ | `POST /api/v1/clientes/{id}/contratos` |
| RF-5.4 Estado calculado por fechas | ✅ | `EstadoContrato.calcular` (aviso a 30 días) |
| RB-3 Contrato asociado a un único cliente | ✅ | `clienteId` obligatorio e inmutable |
| RF-6.1 Auditoría de acciones críticas | ◐ | Logger `AUDITORIA` (sin tabla de auditoría) |
| NFR-SEG-2 bcrypt · NFR-SEG-4 roles · NFR-SEG-9 JWT | ✅ | bcrypt costo 12, roles, JWT HS256 de 15 min |
| NFR-SEG-5 Validación y saneamiento, CSRF, XSS, SQLi | ✅ | Bean Validation, JPA parametrizado, cabeceras CSP, API sin cookies |
| NFR-SEG-6 Logs seguros | ✅ | Errores genéricos al cliente; logs sin datos personales |
| NFR-PERF-1 CRUD < 300 ms · NFR-PERF-3 200 usuarios | ✅ | Gatling (CI) y JMeter (200 usuarios) |
| NFR-USAB-3 Mensajes de error amigables | ✅ | `ErrorResponse` JSON con detalle por campo |
| NFR-USAB-4 Primero clientes, luego contratos | ✅ | `/clientes/{id}/contratos` |
| RF-2 Registro y empresas · RF-3.1/3.3 gestión de usuarios · RB-1/RB-2 multiempresa | ✗ | Fuera del alcance del MVP (casos diseñados, no ejecutados) |
| RF-5.2, 5.3, 5.5 Documentos y enlaces | ✗ | Fuera del alcance del MVP |
| NFR-SEG-1 TLS · NFR-SEG-3 cifrado en reposo · NFR-SEG-7 respaldos | ✗ | Se resuelven en el despliegue (proxy TLS, PostgreSQL cifrado) |
| NFR-PERF-2, NFR-USAB-1/2, NFR-COMPAT | ✗ | Requieren frontend (no incluido en el MVP) |

## Requisitos

- Java 21
- Maven 3.9+ (o el wrapper `./mvnw`)
- Docker (solo para SonarQube local, Trivy y ZAP por consola)

## Ejecutar

```bash
./mvnw spring-boot:run
```

| Recurso    | URL                                          |
| ---------- | -------------------------------------------- |
| Home       | http://localhost:8080/                       |
| Swagger UI | http://localhost:8080/swagger-ui.html        |
| Redoc      | http://localhost:8080/redoc.html             |
| OpenAPI    | http://localhost:8080/openapi/atlas-openapi.yaml |

La base SQLite se crea en `./data/atlas.db` con dos usuarios y datos de demostración
(3 clientes y 3 contratos, uno en cada estado).

| Usuario  | Contraseña por defecto | Rol    | Variable para cambiarla |
| -------- | ---------------------- | ------ | ----------------------- |
| `admin`  | `Admin#Atlas2026`      | ADMIN  | `ATLAS_ADMIN_PASSWORD`  |
| `editor` | `Editor#Atlas2026`     | EDITOR | `ATLAS_EDITOR_PASSWORD` |

> Las contraseñas por defecto son **solo para entorno local y CI**. En cualquier
> despliegue se definen con variables de entorno, igual que `ATLAS_JWT_SECRET`
> (Base64 de 256 bits o más; si no se define, se genera una clave aleatoria en cada arranque).

## Tests

```bash
./mvnw test      # JUnit + Mockito + Cucumber + integración con Spring Security
./mvnw verify    # build completo: formato, convenciones, pruebas, PMD, SpotBugs, JaCoCo y PIT
```

## Seguridad implementada

| Control | Dónde | Requisito |
| --- | --- | --- |
| Contraseñas con bcrypt (costo 12) | `SecurityConfig.passwordEncoder` | NFR-SEG-2 |
| JWT HS256, 15 minutos, `jti` único | `JwtService` | RF-1.1, NFR-SEG-9 |
| Logout con lista de revocación | `JwtService.revocar` | RF-1.2 |
| Bloqueo 15 min tras 5 intentos fallidos | `AuthUseCase` | Fuerza bruta (OWASP A07) |
| Mismo mensaje y mismo costo si el usuario no existe | `AuthUseCase` (hash ficticio) | Enumeración de usuarios |
| Roles: lectura ADMIN/EDITOR, escritura solo ADMIN | `SecurityConfig` | NFR-SEG-4, RB-4, RB-5 |
| Validación de entradas (RUT, correo, teléfono, sin `<` `>`) | DTOs `*Request` | NFR-SEG-5, RF-4.3 |
| Consultas parametrizadas (Spring Data JPA) | `*JpaRepository` | SQL Injection |
| Cabeceras CSP, X-Frame-Options, nosniff, Referrer-Policy | `SecurityHeadersFilter` | XSS, clickjacking |
| Errores sin trazas ni mensajes internos | `ApiExceptionHandler`, `server.error.*` | NFR-SEG-6 |
| Log de auditoría sin datos personales, con saneamiento CRLF | `AUDITORIA` en casos de uso | RF-6.1, CWE-117 |
| Imagen Docker sin root y con parches del SO | `Dockerfile` | Contenedores |

---

## Calidad de código con Spotless

- **`pom.xml`**: `spotless-maven-plugin` con Google Java Format 1.22.0,
  `removeUnusedImports`, `trimTrailingWhitespace` y `endWithNewline` (fase `validate`).
- **GitHub Actions**: job `spotless`.

```bash
./mvnw spotless:check   # verifica el formato
./mvnw spotless:apply   # corrige el formato automáticamente
```

## Calidad de código con Checkstyle

- **`pom.xml`**: `maven-checkstyle-plugin` con `checkstyle.xml` (fase `validate`).
- **GitHub Actions**: job `checkstyle`.

```bash
./mvnw checkstyle:check
```

Reglas: líneas de hasta 120 caracteres, sin tabulaciones, salto de línea final, sin
imports `*` ni sin usar, llaves obligatorias y convenciones de nombres.

## Calidad de código con PMD

- **`pom.xml`**: `maven-pmd-plugin` con `pmd-ruleset.xml` (fase `verify`).
- **GitHub Actions**: job `pmd` (sube `pmd-report`).

```bash
./mvnw pmd:check
```

Resultado: **0 violaciones**.

## Calidad de código con SpotBugs

- **`pom.xml`**: `spotbugs-maven-plugin` (esfuerzo máximo, umbral bajo) con `spotbugs-exclude.xml`.
- **GitHub Actions**: job `spotbugs` (sube `spotbugs-report`).

```bash
./mvnw compile spotbugs:check
```

Hallazgo corregido durante el desarrollo: **CT_CONSTRUCTOR_THROW** en `JwtService`
(el constructor valida el secreto y puede lanzar una excepción, lo que deja el objeto
expuesto a *finalizer attacks*). Se corrigió declarando la clase `final`, sin agregar
exclusiones. Resultado final: **0 bugs**.

## Pruebas unitarias con JUnit + Mockito

```bash
./mvnw test
```

| Capa | Clases de prueba | Qué cubren |
| --- | --- | --- |
| Dominio | `EstadoContratoTest`, `RutTest`, `ModeloTest`, `ExcepcionesTest` | Límites del estado (30 días), RUT módulo 11, modelos |
| Aplicación | `ClienteUseCaseTest`, `ContratoUseCaseTest`, `AuthUseCaseTest` | Reglas de negocio, duplicados, fechas, bloqueo y su expiración |
| Persistencia | `MappersTest`, `GatewayImplTest` | Adaptadores JPA con repositorios mockeados |
| Seguridad | `JwtServiceTest`, `JwtAuthFilterTest`, `RespuestaErrorSeguridadTest` | Token expirado, alterado, revocado, clave débil; 401/403 |
| REST | `ClienteControllerTest`, `ContratoControllerTest`, `AuthControllerTest`, `DtoTest` | 200/201/204/400/401/404/405/409/429/500 |
| Configuración | `SecurityHeadersFilterTest`, `ConfigTest`, `DatosInicialesTest` | Cabeceras, OpenAPI, carga inicial |
| Integración | `SeguridadIntegracionTest` | App completa con Spring Security, JWT y SQLite real |

Resultado: **151 pruebas, 0 fallas** (incluye los 11 escenarios de Cucumber).

## Pruebas de mutación con PIT

- **`pom.xml`**: `pitest-maven` sobre `application.usecase.*` y `domain.model.*`;
  umbral de mutación **80 %**.
- **GitHub Actions**: job `mutation` (sube `pit-report`).

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
```

Resultado: **145 mutantes, 140 eliminados (97 %)**; *test strength* 97 %.

## Cobertura con JaCoCo

- **`pom.xml`**: `jacoco-maven-plugin` con regla mínima de **80 % de líneas**
  (se excluye `AtlasApplication`).
- **GitHub Actions**: job `jacoco` (sube `jacoco-report`).

```bash
./mvnw verify    # reporte en target/site/jacoco/index.html
```

Resultado: **99,9 % de líneas (702/703) y 98,2 % de ramas**.

> 99,9 % de cobertura no significa 99,9 % de calidad: la fuerza de las pruebas se mide
> con PIT.

## Análisis con SonarQube

**En local** (SonarQube Community con Docker, igual que el proyecto base):

```bash
docker compose up -d       # http://localhost:9000 (admin / admin)
./mvnw clean verify
./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN
```

**En GitHub Actions** (SonarCloud): crear los secretos `SONAR_TOKEN` y
`SONAR_HOST_URL=https://sonarcloud.io`, y la variable `SONAR_ORGANIZATION`
(Settings → Secrets and variables → Actions → *Variables*). Sin ellos el job pasa con
el aviso *pendiente*. Paso a paso en [`githubAction.md`](githubAction.md).

## Seguridad de dependencias con Snyk

```bash
npm install -g snyk
snyk auth TU_TOKEN
snyk test --severity-threshold=critical
```

En GitHub Actions se activa con el secreto `SNYK_TOKEN`. El archivo `.snyk` es el del
proyecto base (acepta como riesgo temporal, con vencimiento, las críticas transitivas de
Spring Boot 3.3.5).

> **Hallazgo y corrección (INC-01 del informe):** el reporte completo de Trivy mostró
> 9 CVE CRITICAL y 30 HIGH en las librerías de Spring Boot 3.3.5 (Tomcat 10.1.31,
> Jackson 2.17.2, Spring Security 6.3.4, Spring Framework 6.1.14). Se actualizó a
> **Spring Boot 3.5.16** con **Tomcat 10.1.58** y **Jackson 2.21.7**, y springdoc a 2.8.9.
> Riesgo residual aceptado: CVE-2026-47884 y CVE-2026-47890 (spring-webmvc 6.2.19) solo se
> corrigen en Spring Framework 7 / Spring Boot 4 (migración planificada).

## Integración / BDD con Cucumber

Escenarios en español en `src/test/resources/features/`:

| Feature | Escenarios |
| --- | --- |
| `gestion_clientes.feature` | Registrar cliente, RUT duplicado, RUT inválido, eliminación con confirmación |
| `estado_contrato.feature` | Esquema con 4 fechas límite (EXPIRADO, POR_VENCER, ACTIVO) y fechas invertidas |
| `autenticacion.feature` | Login correcto y bloqueo tras 5 intentos |

```bash
./mvnw test -Dtest=CucumberRunnerTest    # reporte en target/cucumber-report.html
```

Resultado: **11 escenarios, 0 fallas**.

## Contenedor con Docker y Trivy

`Dockerfile` multi-stage: compilación con `maven:3.9-eclipse-temurin-21` y ejecución en
`eclipse-temurin:21-jre-alpine` con usuario **no root** y `apk upgrade`.

```bash
docker build -t atlas-api:1.0.0 .
docker run --rm -p 8080:8080 atlas-api:1.0.0

docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image --scanners vuln --pkg-types os \
  --severity CRITICAL,HIGH --exit-code 1 atlas-api:1.0.0
```

El job `container` falla si hay CVE **CRITICAL/HIGH** en el sistema operativo de la
imagen y además publica el reporte completo (SO + librerías) en el resumen del job y
como artefacto `trivy-report`.

## Seguridad dinámica (DAST) con OWASP ZAP

`SecurityHeadersFilter` agrega las cabeceras a **todas** las respuestas (incluidas las
401/403, porque se ejecuta antes que Spring Security):

| Cabecera | Valor | Protege contra |
| --- | --- | --- |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self'; …; frame-ancestors 'none'` | XSS, inyección |
| `X-Frame-Options` | `DENY` | Clickjacking |
| `X-Content-Type-Options` | `nosniff` | Sniffing de contenido |
| `Referrer-Policy` | `no-referrer` | Fuga de referrer |
| `Permissions-Policy` | `geolocation=(), camera=(), microphone=()` | APIs del navegador |
| `Cross-Origin-Opener-Policy` / `-Resource-Policy` | `same-origin` | Aislamiento entre orígenes |

```bash
./mvnw spring-boot:run
# en otra terminal:
docker run --rm --network host -v "$PWD":/zap/wrk/:rw \
  ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
  -t http://localhost:8080 -i -c .zap/rules.tsv -r zap-report.html
```

Redoc se sirve desde la propia aplicación (`/js/redoc.standalone.js`, MIT), sin CDN: así
la CSP no permite scripts de terceros y se eliminaron las alertas 90003 (SRI) y 10017
(JavaScript de otro dominio) que ZAP reportó en la primera ejecución.

`.zap/rules.tsv` marca como **FAIL** la falta de anti-clickjacking (10020),
`nosniff` (10021) y CSP (10038). El escaneo autenticado con ZAP Desktop se explica en
[`docs/PRUEBAS_MANUALES.md`](docs/PRUEBAS_MANUALES.md).

## Pruebas de carga con Gatling

`src/test/java/cl/duoc/atlas/performance/AtlasSimulation.java`: cada usuario inicia
sesión como EDITOR y consulta 5 veces clientes y contratos. Aserciones: **0 errores**
y **p95 < 300 ms** en las consultas (NFR-PERF-1).

```bash
./mvnw spring-boot:run
# en otra terminal:
./mvnw gatling:test              # 10 usuarios (CI)
./mvnw gatling:test -Dvu=50      # más usuarios
```

## Pruebas de carga con JMeter

Plan `src/test/jmeter/atlas.jmx`: login una vez por usuario, luego 5 iteraciones de
`GET /api/v1/clientes` y `GET /api/v1/clientes/{id}/contratos`, con aserción HTTP 200.

```bash
./mvnw spring-boot:run
# en otra terminal (CI usa 50 usuarios):
./mvnw -Pjmeter jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
# prueba del informe (NFR-PERF-3, 200 usuarios):
./mvnw -Pjmeter -Djmeter.usuarios=200 -Djmeter.rampa=60 jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
```

Resultados de referencia (equipo de 2 núcleos, app y JMeter en la misma máquina):

| Escenario | Peticiones | Errores | p95 login | p95 consultas |
| --- | --- | --- | --- | --- |
| 200 usuarios, rampa 60 s | 2 200 | 0 % | 473 ms | 12 ms |
| 200 usuarios, rampa 20 s (estrés) | 2 200 | 0 % | 28 s | 0,5–1,6 s |

El escenario de estrés muestra que **bcrypt (costo 12) satura la CPU** cuando
200 usuarios inician sesión casi al mismo tiempo. Está registrado como incidente de
rendimiento en el informe.

## Pruebas manuales (Postman, ZAP, JMeter)

La colección `postman/Atlas.postman_collection.json` (42 requests, 82 aserciones)
cubre los casos de prueba del informe (CP-AUT, CP-CLI, CP-CON, CP-ROL, CP-SEG). El
paso a paso para ejecutar y capturar cada prueba está en
[`docs/PRUEBAS_MANUALES.md`](docs/PRUEBAS_MANUALES.md).

---

## Arquitectura

```
cl.duoc.atlas
├── domain                       # Núcleo: no conoce frameworks
│   ├── model                    # Cliente, Contrato, Usuario, EstadoContrato, Rut, Pagina, Sesion
│   ├── gateway                  # Puertos: ClienteGateway, ContratoGateway, UsuarioGateway, TokenGateway
│   └── exception                # Excepciones de negocio
├── application
│   └── usecase                  # ClienteUseCase, ContratoUseCase, AuthUseCase
└── infrastructure               # Detalles: frameworks, BD, HTTP
    ├── adapters/jpa             # Entidades, repositorios, mappers y gateways (SQLite)
    ├── security                 # SecurityConfig, JwtService, JwtAuthFilter, respuestas 401/403
    ├── entrypoints/rest         # Controladores, DTOs y manejo de errores
    └── config                   # OpenAPI, cabeceras de seguridad, reloj, datos iniciales
```

Las dependencias apuntan siempre **hacia el dominio**: la infraestructura implementa
los puertos que el dominio define.

## Endpoints

| Método | Ruta | Descripción | Rol |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Inicia sesión y entrega el JWT | Público |
| POST | `/api/v1/auth/logout` | Revoca el token actual | Autenticado |
| GET | `/api/v1/clientes?nombre=&pagina=0&tamano=10` | Lista clientes activos | ADMIN, EDITOR |
| GET | `/api/v1/clientes/{id}` | Obtiene un cliente | ADMIN, EDITOR |
| POST | `/api/v1/clientes` | Crea un cliente | ADMIN |
| PUT | `/api/v1/clientes/{id}` | Actualiza un cliente | ADMIN |
| DELETE | `/api/v1/clientes/{id}?confirmar=true` | Eliminación lógica | ADMIN |
| GET | `/api/v1/clientes/{id}/contratos` | Contratos del cliente con estado | ADMIN, EDITOR |
| POST | `/api/v1/clientes/{id}/contratos` | Crea un contrato | ADMIN |
| GET | `/api/v1/contratos/{id}` | Obtiene un contrato | ADMIN, EDITOR |
| PUT | `/api/v1/contratos/{id}` | Actualiza un contrato | ADMIN |
| DELETE | `/api/v1/contratos/{id}` | Elimina un contrato | ADMIN |

### Ejemplo

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"admin","contrasena":"Admin#Atlas2026"}' | jq -r .accessToken)

curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"rut":"12.345.678-5","nombre":"Ferretería Los Andes","direccion":"Av. Matta 500",
       "email":"contacto@losandes.cl","telefono":"+56912345678"}'
```

## Trabajo futuro (fuera del MVP)

- Registro de usuarios, empresas y gestión de usuarios por empresa (RF-2, RF-3.1, RF-3.3).
- Documentos por contrato y enlaces para compartir (RF-5.2, RF-5.3, RF-5.5).
- PostgreSQL con cifrado de datos sensibles y respaldos (NFR-SEG-3, NFR-SEG-7).
- TLS en el despliegue (NFR-SEG-1) y frontend accesible (NFR-USAB-1/2, NFR-COMPAT).
- Tabla de auditoría consultable y lista de revocación persistente.
