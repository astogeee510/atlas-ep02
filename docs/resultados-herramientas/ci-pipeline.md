# Resultados del pipeline (GitHub Actions)

Repositorio `astogeee510/atlas-ep02` · 16-10-2026

| Ejecución | Resultado | Motivo |
| --- | --- | --- |
| 38865856363 | Falló en el job 15 | Trivy: la imagen base `nginx:1.29-alpine` tenía vulnerabilidades HIGH con parche |
| 38867412590 | **20/20 jobs en verde** | Se agregó `apk upgrade --no-cache` a los Dockerfile |

## Métricas de la ejecución 38867412590

| Job | Herramienta | Resultado |
| --- | --- | --- |
| 01–03 | Spotless · Checkstyle · PMD | 0 violaciones |
| 04 | SpotBugs (Max/Low) | 0 bugs |
| 05 | JUnit + Mockito + Testcontainers + Cucumber | 172 pruebas, 0 fallas (8 de integración con PostgreSQL 16) |
| 06 | PIT | mutation score 84,1 % (227/270; 35 sobrevivientes, 8 sin cobertura) |
| 07 | JaCoCo | 94,8 % de líneas (1.116/1.177) |
| 08–09 | SonarQube · Snyk | omitidos: faltan `SONAR_TOKEN` / `SNYK_TOKEN` |
| 11–14 | ESLint · Vitest · npm audit · Vite | 0 errores · 25 pruebas · 0 vulnerabilidades de producción · build OK |
| 15 | Trivy | base nginx: 44 HIGH → atlas-web: 0 · atlas-api: 0 |
| 16 | Cypress (Chrome) | 4/4 pruebas aprobadas |
| 17 | OWASP ZAP baseline | 0 altas · 1 media (CSP style-src unsafe-inline) · 4 bajas (COEP, COOP, CORP, timestamp) |
| 18 | Lighthouse CI móvil | Performance 79 · Accessibility 100 · Best Practices 96 · LCP 3,7 s |
| 19 | Gatling (10 usuarios) | 110 peticiones · 0 errores · p95 listar clientes 13 ms · media 38 ms |
| 20 | JMeter (50 usuarios × 3) | 500 peticiones · 0 % error · promedio 41 ms · máximo 601 ms |

## Vulnerabilidades de la imagen base nginx:1.29-alpine (Trivy, HIGH con parche)

curl/libcurl 8.17.0-r1 → 8.22.0-r0 (CVE-2026-11352, CVE-2026-11586, CVE-2026-12064, CVE-2026-5773, CVE-2026-6276,
CVE-2026-8286, CVE-2026-8458, CVE-2026-8925, CVE-2026-8927, CVE-2026-9547) · libssl3/libcrypto3 3.5.6-r0 → 3.5.8-r0
(CVE-2026-14456, CVE-2026-45447) · libexpat 2.7.5-r0 → 2.8.5-r0 (CVE-2026-45186, CVE-2026-66046, CVE-2026-76641,
CVE-2026-76956, CVE-2026-76957, CVE-2026-93990) · pcre2 10.47-r0 → 10.49-r0 (CVE-2026-103111, CVE-2026-86145,
CVE-2026-89157, CVE-2026-89161) · libuuid 2.41.4-r0 → 2.41.6 (6 CVE) · libxml2 (CVE-2026-6732) · nghttp2-libs
(CVE-2026-27135) · c-ares (CVE-2026-33630) · tiff (CVE-2026-4775).
