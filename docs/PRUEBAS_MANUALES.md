# Pruebas manuales y capturas para el informe

Guía para ejecutar en el computador del equipo las pruebas que **no** corren en el
pipeline (o que se ejecutan con más carga), y para tomar las capturas que pide el
informe. Cada paso indica **qué caso de prueba (CP)** cubre y **dónde va la captura**
en el informe (sección 5 *Ejecución* y Anexo B *Evidencias*).

> Tiempo estimado: 1,5 a 2 horas en total.

## 0. Preparación (una vez)

| Herramienta | Versión | Descarga |
| --- | --- | --- |
| Java | 21 (Temurin) | <https://adoptium.net> |
| Git | cualquiera | <https://git-scm.com> |
| Postman | Desktop | <https://www.postman.com/downloads> |
| OWASP ZAP | 2.15 o superior (Desktop) | <https://www.zaproxy.org/download> |
| Apache JMeter | 5.6.3 (requiere Java) | <https://jmeter.apache.org/download_jmeter.cgi> |
| DB Browser for SQLite (opcional) | cualquiera | <https://sqlitebrowser.org> |

```bash
git clone https://github.com/astogeee510/atlas-ep02.git
cd atlas-ep02
./mvnw spring-boot:run          # en Windows: mvnw.cmd spring-boot:run
```

Cuando el log muestre `Started AtlasApplication`, abrir <http://localhost:8080>.

**Captura 1 (Anexo B):** la página Home de Atlas.
**Captura 2:** Swagger UI en <http://localhost:8080/swagger-ui.html> con los tres grupos
(Autenticación, Clientes, Contratos).

Para reiniciar la base de datos entre pruebas: detener la app y borrar `data/atlas.db`.

---

## 1. Postman — pruebas funcionales y de seguridad (CP-AUT, CP-ROL, CP-CLI, CP-CON, CP-SEG)

1. Postman → **Import** → seleccionar los dos archivos de la carpeta `postman/`:
   - `Atlas.postman_collection.json`
   - `Atlas-local.postman_environment.json`
2. Arriba a la derecha, seleccionar el entorno **Atlas local**.
3. En la colección **Atlas MVP - Casos de prueba EP02** → botón **Run** →
   dejar el orden por defecto, **1 iteración** → **Run Atlas MVP...**

Resultado esperado: **42 requests y 82 tests aprobados** (0 fallidos).

| Captura | Qué mostrar | Caso | Sección del informe |
| --- | --- | --- | --- |
| 3 | Resumen del Runner con todos los tests en verde | Todos | 5.1 Resumen |
| 4 | `CP-AUT-01 Login ADMIN`: respuesta con `accessToken` y `rol: ADMIN` | CP-AUT-01 | 5.2 |
| 5 | `CP-AUT-03 Bloqueo tras 5 intentos fallidos`: **429** y mensaje de 15 minutos | CP-AUT-03 | 5.2 |
| 6 | `CP-AUT-04 El token revocado ya no sirve`: **401** después del logout | CP-AUT-04 | 5.2 |
| 7 | `CP-ROL-02 EDITOR no puede crear clientes`: **403** con el mensaje de permiso | CP-ROL-02 | 5.2 |
| 8 | `CP-CLI-04 Entrada con script (XSS)`: **400** con `details` por campo | CP-CLI-04 | 5.2 |
| 9 | `CP-CON-04 Listar contratos del cliente`: los 3 estados (EXPIRADO, POR_VENCER, ACTIVO) | CP-CON-02 | 5.2 |
| 10 | `CP-CLI-07 Eliminar sin confirmar`: **400** pidiendo `confirmar=true` | CP-CLI-07 | 5.2 |
| 11 | `CP-SEG-03 Cabeceras de seguridad en la Home`: pestaña **Headers** | CP-SEG-03 | 5.2 |

> Para cada captura individual: abrir el request en la colección, pulsar **Send** y
> capturar la respuesta junto con la pestaña **Test Results**.

### Exportar la evidencia
En el Runner: **Export Results** → guardar el JSON en `docs/evidencias/postman-run.json`
(opcional, como respaldo de las capturas).

---

## 2. Verificar el hash de contraseñas en la base (CP-SEG-01)

Con la app detenida o en ejecución, abrir `data/atlas.db` con **DB Browser for SQLite**
→ pestaña **Browse Data** → tabla `usuarios`.

**Captura 12:** la columna `hash_contrasena` con valores que empiezan por `$2a$12$`
(hash bcrypt de costo 12, nunca la contraseña en claro).

Alternativa por consola: `sqlite3 data/atlas.db "select nombre_usuario, hash_contrasena, rol from usuarios;"`

> Este caso también está automatizado en `SeguridadIntegracionTest` (pipeline).

---

## 3. OWASP ZAP — escaneo autenticado (CP-SEG-02, CP-SEG-04)

El pipeline ya ejecuta un **baseline pasivo** sin autenticación. Aquí se hace el
**escaneo activo autenticado**, que ataca los endpoints protegidos.

1. Con la app levantada, obtener un token de ADMIN (request `CP-AUT-01 Login ADMIN` en
   Postman) y copiar el valor de `accessToken`.
2. Abrir ZAP Desktop → *No, I do not want to persist this session* → **Start**.
3. **Tools → Options → Replacer → Add**:
   - Description: `JWT Atlas`
   - Match Type: **Request Header (will add if not present)**
   - Match String: `Authorization`
   - Replacement String: `Bearer <pegar el token>`
   - Marcar **Enable** → OK.
   > El token dura **15 minutos**. Si el escaneo se alarga, repetir el login y actualizar el Replacer.
4. **Import → Import an OpenAPI definition from a URL**:
   `http://localhost:8080/openapi/atlas-openapi.yaml` (Target URL: `http://localhost:8080`).
5. En el árbol **Sites**, clic derecho sobre `http://localhost:8080` → **Attack → Active Scan**
   → Policy *Default Policy* → **Start Scan**. Esperar a que llegue a 100 %.
6. **Report → Generate Report** → plantilla *Traditional HTML Report* → guardar como
   `docs/evidencias/zap-activo.html`.

| Captura | Qué mostrar | Sección |
| --- | --- | --- |
| 13 | Pestaña **Alerts** con la cantidad de alertas por riesgo (High / Medium / Low / Informational) | 5.2 y 5.3 |
| 14 | Detalle de la alerta de mayor riesgo (si existe) con su descripción y solución | 5.3 Incidentes |
| 15 | Portada del reporte HTML con el resumen | Anexo B |

**Qué registrar en el informe:** cantidad de alertas por nivel de riesgo. Cada alerta
**High** o **Medium** se registra como incidente en 5.3, con su criticidad
(High → CRÍTICO/GRAVE, Medium → MODERADO, Low → LEVE).
Criterio de aceptación del ERS: *pruebas de penetración sin hallazgos críticos*.

> Alternativa por consola (escaneo activo con API y token, requiere Docker):
> ```bash
> docker run --rm --network host -v "$PWD":/zap/wrk/:rw ghcr.io/zaproxy/zaproxy:stable \
>   zap-api-scan.py -t http://localhost:8080/openapi/atlas-openapi.yaml -f openapi \
>   -z "-config replacer.full_list(0).description=jwt -config replacer.full_list(0).enabled=true \
>       -config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=Authorization \
>       -config replacer.full_list(0).replacement='Bearer TOKEN'" \
>   -r zap-activo.html
> ```

---

## 4. JMeter — 200 usuarios concurrentes (CP-PERF-02, NFR-PERF-3)

El pipeline ejecuta el plan con 50 usuarios. La prueba del informe usa **200**.

### Opción A: con Maven (más simple)

```bash
# terminal 1
./mvnw spring-boot:run
# terminal 2
./mvnw -Pjmeter -Djmeter.usuarios=200 -Djmeter.rampa=60 \
  jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
```

La consola muestra la línea `summary = ... Err: 0 (0.00%)` y `Failed requests: 0`.

### Opción B: JMeter instalado (para el dashboard HTML)

```bash
jmeter -n -t src/test/jmeter/atlas.jmx -Jusuarios=200 -Jrampa=60 -Jiteraciones=5 \
  -l resultados-200.jtl -e -o reporte-jmeter-200
```

Abrir `reporte-jmeter-200/index.html`.

| Captura | Qué mostrar | Sección |
| --- | --- | --- |
| 16 | Dashboard: tabla **Statistics** (muestras, % error, media, p95, throughput por request) | 5.2 |
| 17 | Gráfico **Response Times Over Time** | Anexo B |
| 18 (opcional) | Misma prueba con `-Jrampa=20` (estrés): muestra cómo el login satura la CPU | 5.3 Incidente de rendimiento |

**Qué registrar en el informe:** total de muestras, % de error, p95 de `listar clientes`
y `contratos del cliente` (umbral **300 ms**) y p95 del `login`. Indicar CPU y RAM del
equipo donde se ejecutó.

---

## 5. Swagger UI — usabilidad de la API (CP-USA-01)

1. Abrir <http://localhost:8080/swagger-ui.html>.
2. Ejecutar `POST /api/v1/auth/login` con `{"nombreUsuario":"editor","contrasena":"Editor#Atlas2026"}`.
3. Copiar el token → botón **Authorize** → pegar → **Authorize**.
4. Ejecutar `POST /api/v1/clientes` con un RUT inválido (`12.345.678-9`).

**Captura 19:** la respuesta 400 con el mensaje en español. **Captura 20:** un `POST` del
editor respondiendo 403. Así se evidencia la documentación de ayuda y los mensajes de
error amigables (NFR-USAB-3).

---

## 6. GitHub Actions (todas las herramientas automáticas)

En <https://github.com/astogeee510/atlas-ep02/actions> → última ejecución de **Calidad de código**:

| Captura | Qué mostrar | Sección |
| --- | --- | --- |
| 21 | Grafo con los 14 jobs en verde | 3.4 Herramientas y 5.1 |
| 22 | Resumen de **Pruebas unitarias** (total de pruebas por clase) | 5.2 |
| 23 | Resumen de **Mutación (PIT)** y **JaCoCo** | 5.2 |
| 24 | Resumen de **Contenedor (Docker + Trivy)** | 5.2 |
| 25 | Resumen de **DAST (OWASP ZAP)** | 5.2 |
| 26 | Resúmenes de **Gatling** y **JMeter** | 5.2 |
| 27 | Jobs **SonarQube** y **Snyk** (con resultados cuando estén los tokens) | 5.2 |

---

## 7. SonarQube y Snyk

Seguir [`githubAction.md`](../githubAction.md), sección 2. Después de crear los
secretos, ejecutar **Actions → Calidad de código → Run workflow** y capturar:

- **Captura 28:** dashboard de SonarCloud (Quality Gate, Bugs, Vulnerabilities, Code Smells,
  Coverage, Duplications, Security Hotspots).
- **Captura 29:** log del job Snyk con `✔ Tested N dependencies` o la lista de vulnerabilidades.

Cada vulnerabilidad o *Security Hotspot* relevante se registra como incidente en 5.3.

---

## 8. Checklist final

- [ ] Postman: 42 requests y 82 tests en verde (capturas 3 a 11).
- [ ] Hash bcrypt en la base (captura 12).
- [ ] ZAP activo autenticado y reporte HTML (capturas 13 a 15).
- [ ] JMeter con 200 usuarios (capturas 16 y 17; opcional 18).
- [ ] Swagger con mensajes de error (capturas 19 y 20).
- [ ] GitHub Actions con los 14 jobs (capturas 21 a 27).
- [ ] SonarCloud y Snyk con token (capturas 28 y 29).
- [ ] Pegar las capturas en el informe donde dice **[CAPTURA n]** y completar los
      campos marcados en amarillo.
