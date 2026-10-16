# Evidencias EP02 · Atlas

Guarde aquí cada captura con el **nombre exacto** de la tabla. Reglas (guía de herramientas §9): que se vea el
dato que valida el caso (código HTTP, estado, hallazgo, CVE), la URL o endpoint, la **fecha** y el **ejecutor**;
legible sin zoom y con un recuadro sobre el dato clave.

| Anexo | Archivo | Debe mostrar | Cómo |
| --- | --- | --- | --- |
| 0.1 | Anexo_0.1_ambiente_docker-compose-ps.png | `docker compose ps` con db, backend y frontend "Up (healthy)" | Terminal |
| 0.1b | Anexo_0.1b_ambiente_login-https.png | Login en https://localhost | Script |
| 1.1 | Anexo_1.1_CP-FUN-01_registro-empresa-admin.png | 201 del registro, 201 de la empresa y rol ADMIN en `/auth/me` | Postman |
| 1.1b | Anexo_1.1_CP-FUN-01_dashboard-admin.png | Dashboard con indicadores | Script |
| 1.2 | Anexo_1.2_CP-FUN-02_login-logout-401.png | 401 con clave mala, 200 con JWT, 204 logout y 401 con el token revocado | Postman Runner |
| 1.3 | Anexo_1.3_CP-FUN-03_clientes-filtro-validacion.png | 201, 400 por RUT y por teléfono, lista filtrada | Postman + Script |
| 1.4 | Anexo_1.4_CP-FUN-04_confirmacion-eliminacion.png | Diálogo de confirmación + `SELECT eliminado FROM cliente` = true | Script + psql |
| 1.5 | Anexo_1.5_CP-FUN-05_estados-contrato.png | Chips "Activo / Por vencer / Expirado" | Script |
| 1.6 | Anexo_1.6_CP-FUN-06_gestion-usuarios.png | Alta de EDITOR, cambio de rol y baja | Script + Postman |
| 2.1 | Anexo_2.1_CP-INT-01_documento-subida-descarga.png | 201 con sha256 y tiempo < 10 s; fila en `documento` | Postman + psql |
| 2.2 | Anexo_2.2_CP-INT-02_enlace-compartido.png | 201 enlace, 200 descarga pública, 204 revocar, 410 | Postman |
| 2.3 | Anexo_2.3_CP-INT-03_auditoria.png | Página de auditoría con las acciones | Script |
| 2.4 | Anexo_2.4_CP-INT-04_aislamiento-empresas.png | 403 de admin.otra a la empresa 1 y 409 por correo repetido | Postman |
| 3.1 | Anexo_3.1_CP-SEG-01_rbac-editor-403.png | 403 en POST/DELETE con token de editor; 401 sin token | Postman |
| 3.1b | Anexo_3.1b_CP-SEG-01_editor-sin-acciones.png | Vista de editor sin botones de escritura | Script |
| 3.2 | Anexo_3.2_CP-SEG-02_reporte-zap.png | Alertas de ZAP con su riesgo + 400 a XSS/SQLi en Postman | ZAP + Postman |
| 3.3 | Anexo_3.3_CP-SEG-03_tls-bcrypt-cifrado.png | Certificado/TLS 1.3, hash `$2a$12$...`, `numero_cuenta` = `v1:...` | openssl + psql |
| 3.4 | Anexo_3.4_CP-SEG-04_logs-backup-restore.png | `docker compose logs backend` sin contraseñas + restore con conteos iguales | Terminal |
| 3.5 | Anexo_3.5_CP-SEG-05_archivo-malicioso.png | 415 al subir `malicioso.pdf` | Postman |
| 3.6 | Anexo_3.6_CP-SAST-01_sast-sca-trivy.png | Sonar, ESLint, Snyk, npm audit y Trivy | Navegador + CI |
| 4.1 | Anexo_4.1_CP-PERF-01_jmeter-200-usuarios.png | Thread Group 200 hilos + Aggregate Report (p95, error %) | JMeter |
| 4.2 | Anexo_4.2_CP-PERF-02_lighthouse-movil.png | Performance y LCP en móvil | Chrome DevTools |
| 5.1 | Anexo_5.1_CP-USA-01_movil-375x667.png | Vista 375×667 sin scroll horizontal + Accessibility | Script + Lighthouse |
| 5.2 | Anexo_5.2_CP-COMPAT-01_matriz-navegadores.png | Cypress verde en Chrome, Firefox y Edge (+ Safari manual) | Cypress |
| 5.3 | Anexo_5.3_CP-REG-01_cypress-regresion.png | Suite E2E en verde | Cypress / CI |
| 6.1 | Anexo_6.1_CP-DIS-01_uptime-flyway.png | Salida de `scripts/uptime.sh` y tabla `flyway_schema_history` | Terminal |
| 7.1 | Anexo_7.1_pipeline-actions-verde.png | Ejecución con los 20 jobs | GitHub Actions |
| 7.2 | Anexo_7.2_pipeline-artefactos.png | Artefactos descargables | GitHub Actions |
| 7.3 | Anexo_7.3_jacoco-cobertura.png | % de líneas cubiertas | Reporte HTML |
| 7.4 | Anexo_7.4_pit-mutacion.png | Mutation score | Reporte HTML |
| 7.5 | Anexo_7.5_gatling-reporte.png | Tiempos y errores de Gatling | Reporte HTML |
| 7.6 | Anexo_7.6_swagger-endpoints.png | Swagger UI con los endpoints | Script |

Comandos útiles:

```bash
# CP-SEG-03: TLS
openssl s_client -connect localhost:443 -servername localhost </dev/null 2>/dev/null | grep -E "Protocol|Cipher"
# CP-SEG-03: hash bcrypt y columna cifrada
docker compose -f infra/docker-compose.yml exec db psql -U atlas -d atlas \
  -c "SELECT nombre_usuario, left(password_hash, 29) FROM usuario;" \
  -c "SELECT id, nombre, left(numero_cuenta, 40) FROM cliente WHERE numero_cuenta IS NOT NULL;"
# CP-FUN-04: eliminación lógica
docker compose -f infra/docker-compose.yml exec db psql -U atlas -d atlas -c "SELECT id, nombre, eliminado, eliminado_en FROM cliente ORDER BY id;"
# CP-DIS-01: migraciones aplicadas
docker compose -f infra/docker-compose.yml exec db psql -U atlas -d atlas -c "SELECT version, description, success FROM flyway_schema_history;"
```
