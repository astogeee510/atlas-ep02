# Cómo completar el informe EP02 (Atlas)

El informe `Informe_EP02_CasoA_Atlas.docx` ya trae completas las secciones 1 a 5, 7 y 8, más los resultados
reales de la **etapa 1** (análisis estático, pruebas del frontend y Lighthouse). Lo que está **resaltado en
amarillo entre corchetes** solo lo puede aportar el equipo.

## 1. Datos del equipo
- Portada: nombres completos, sección, docente, fecha y URL del repositorio.
- 1.7: comentarios del docente en la EP01 y el cambio aplicado (filas 1 y 2).
- 1.4: indicar si el docente aprobó usar Spring Boot en vez de Express.
- 3.5: nombres de los responsables.

## 2. Ejecutar la etapa 2 (en este orden)
1. Subir el repositorio a GitHub y revisar la pestaña **Actions** (captura 7.1 y 7.2). Opcional: secretos
   `SONAR_TOKEN`, `SONAR_HOST_URL`, `SONAR_ORGANIZATION` y `SNYK_TOKEN`.
2. `bash scripts/generar-env.sh` y `docker compose -f infra/docker-compose.yml up -d --build` (captura 0.1).
3. Postman: importar la colección y el ambiente de `postman/`, desactivar la verificación SSL, fijar el
   working directory en la raíz del repo y ejecutar el **Runner** (capturas 1.x, 2.x, 3.1, 3.5).
4. `cd scripts/capturas && npm run instalar && EJECUTOR="Su nombre" npm run capturar` (capturas de la interfaz).
5. ZAP (3.2), psql/openssl (3.3), backup/restore (3.4), JMeter 200 usuarios (4.1), Lighthouse en
   https://localhost (4.2 y 5.1), Cypress por navegador (5.2 y 5.3) y `scripts/uptime.sh` (6.1).
   Los comandos están en el README §7 y en `evidencias/LEEME.md`.

## 3. Volcar los resultados en el informe
- 6.2: para cada caso, Aprobado / Fallido / Observado, el anexo, el incidente y la fecha + ejecutor.
- 6.1: contar los estados y calcular porcentajes sobre 22 casos.
- Métricas: p95 de JMeter, LCP en QA, tiempo de subida/descarga y disponibilidad.
- 5.1 y 5.2 (fila SAST-15): resultados de SpotBugs, SonarQube, Snyk y Trivy del pipeline.
- 6.3, 7.2 y 8: agregar INC-08 en adelante con su criticidad (escala del informe), norma y mejora.
- 6.4: si se hizo la prueba con 5 usuarios y si se probó Safari.
- 9: conclusión final y veredicto de liberación.
- 10: insertar cada captura bajo su anexo (Insertar → Imagen) y borrar el texto "[insertar captura]".

## 4. Formato antes de subir a AVA
- Revisar que no quede texto resaltado en amarillo (Inicio → Buscar → Formato → Resaltado).
- Títulos Arial 12, cuerpo Arial 11, interlineado 1,5, justificado (ya configurado).
- Guardar como Word y PDF; subir el archivo directamente a AVA (no un enlace).
