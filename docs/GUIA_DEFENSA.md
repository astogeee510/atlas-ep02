# Guía de defensa del MVP Atlas

Resumen para que cada integrante pueda explicar el código y el pipeline en la
presentación (10 minutos + 5 de preguntas).

## 1. Recorrido de una petición (para explicar en 1 minuto)

`POST /api/v1/clientes` con el token de un ADMIN:

1. **`SecurityHeadersFilter`** agrega las cabeceras de seguridad a la respuesta.
2. **`JwtAuthFilter`** lee `Authorization: Bearer <token>` y le pide a **`JwtService`**
   que valide firma, expiración y que no esté revocado. Si es válido, deja al usuario
   con `ROLE_ADMIN` en el contexto.
3. **`SecurityConfig`** decide: POST en `/api/v1/**` solo para ADMIN → si es EDITOR,
   **`RespuestaErrorSeguridad`** responde 403.
4. **`ClienteController`** valida el JSON con las anotaciones de **`ClienteRequest`**
   (RUT, correo, teléfono, sin `<` `>`) → si falla, 400 con detalle por campo.
5. **`ClienteUseCase.crear`** aplica las reglas: RUT con dígito verificador
   (**`Rut`**), sin duplicados, queda activo; escribe la línea de **AUDITORIA**.
6. **`ClienteGatewayImpl`** traduce con **`ClienteMapper`** y guarda con
   **`ClienteJpaRepository`** (consultas parametrizadas en SQLite).
7. **`ApiExceptionHandler`** convierte cualquier excepción en un JSON claro.

## 2. Dónde está cada requisito

| Requisito | Clase | Prueba que lo demuestra |
| --- | --- | --- |
| RF-1.1 Login / NFR-SEG-9 JWT | `AuthUseCase.login`, `JwtService.emitir` | `AuthUseCaseTest`, `JwtServiceTest`, Postman CP-AUT-01 |
| RF-1.2 Logout | `AuthController.logout`, `JwtService.revocar` | `SeguridadIntegracionTest.logoutRevocaElToken`, CP-AUT-04 |
| Bloqueo por fuerza bruta | `AuthUseCase` (`MAX_INTENTOS`, `BLOQUEO`) | `AuthUseCaseTest`, `autenticacion.feature`, CP-AUT-03 |
| RF-3.2 / RB-4 / RB-5 Roles | `SecurityConfig.filterChain` | `SeguridadIntegracionTest.editorSoloPuedeLeer`, CP-ROL-02 |
| RF-4.1 / 4.2 Clientes | `ClienteUseCase` | `ClienteUseCaseTest`, `gestion_clientes.feature` |
| RF-4.3 Validaciones | `ClienteRequest`, `Rut` | `RutTest`, `ClienteControllerTest`, CP-CLI-03/04 |
| Eliminación lógica con confirmación | `ClienteUseCase.eliminar` | `gestion_clientes.feature`, CP-CLI-07 |
| RF-5.1 / RB-3 Contratos | `ContratoUseCase.crear` | `ContratoUseCaseTest`, CP-CON-01 |
| RF-5.4 Estado | `EstadoContrato.calcular` | `EstadoContratoTest` (límites), `estado_contrato.feature` |
| RF-6.1 Auditoría | Logger `AUDITORIA` en los casos de uso | Log de la app |
| NFR-SEG-2 bcrypt | `SecurityConfig.passwordEncoder` (costo 12) | `lasContrasenasSeGuardanComoHashBcryptCosto12` |
| NFR-SEG-5 XSS / SQLi / clickjacking | DTOs, JPA, `SecurityHeadersFilter` | CP-CLI-04, CP-SEG-02, ZAP |
| NFR-SEG-6 Errores seguros | `ApiExceptionHandler.handleGeneric`, `server.error.*` | `AuthControllerTest.errorInesperado...` |
| NFR-PERF-1 / 3 | — | Gatling (CI), JMeter 200 usuarios |

## 3. Pipeline en 30 segundos

"El pipeline tiene 14 jobs encadenados con `needs`. Primero va lo más barato:
**formato y convenciones** (Spotless, Checkstyle), luego **análisis estático**
(PMD, SpotBugs), después las **pruebas** (JUnit, mutación con PIT y cobertura con
JaCoCo), la **calidad global y las dependencias** (SonarQube, Snyk), los
**escenarios de negocio** (Cucumber), la **imagen** (Docker + Trivy) y al final lo
que necesita la app levantada: **DAST** con ZAP y **carga** con Gatling y JMeter. Si
un job falla, los siguientes no corren: así un error se detecta en el paso más barato."

## 4. Preguntas probables y respuestas cortas

**¿Por qué un MVP y no todo el ERS?**
La EP02 evalúa el plan y la ejecución de pruebas. El MVP implementa los requisitos
esenciales de seguridad y de negocio (login, roles, clientes, contratos). El resto
tiene casos de prueba **diseñados** en la matriz de cobertura y queda como trabajo futuro.

**¿Por qué JWT y no sesión con cookie?**
La API es *stateless* (requisito NFR-SEG-9). El token dura 15 minutos y lleva un `jti`
único que se puede revocar en el logout.

**¿Por qué desactivaron CSRF?**
CSRF aprovecha que el navegador envía cookies automáticamente. Atlas no usa cookies: el
token viaja en el header `Authorization`, que un sitio externo no puede agregar.

**¿Qué pasa si alguien prueba contraseñas en masa?**
Después de 5 fallos el usuario queda bloqueado 15 minutos (429). Además, si el usuario
no existe se compara igual contra un hash ficticio, para que la respuesta tarde lo mismo
y no se pueda saber qué usuarios existen.

**¿Cómo evitan la inyección SQL?**
Spring Data JPA genera consultas con parámetros enlazados; nunca se concatena SQL. La
prueba CP-SEG-02 envía `' OR '1'='1` en el filtro y devuelve 0 resultados.

**¿Qué diferencia hay entre cobertura y mutación?**
La cobertura (JaCoCo, 99,9 %) dice qué líneas se ejecutaron. La mutación (PIT, 97 %)
cambia la lógica (por ejemplo `<` por `<=`) y verifica que alguna prueba falle: mide si
las pruebas realmente **detectan** errores.

**¿Qué encontró SpotBugs?**
`CT_CONSTRUCTOR_THROW` en `JwtService`: el constructor puede lanzar una excepción si el
secreto es débil. Se corrigió declarando la clase `final`.

**¿Qué encontraron en rendimiento?**
Con 200 usuarios entrando de forma escalonada (60 s), las consultas tienen p95 de 6 ms.
Si los 200 inician sesión casi a la vez (20 s), bcrypt satura la CPU y el login sube a
segundos. Es un compromiso consciente: bcrypt es lento a propósito. La mitigación es
escalar horizontalmente o limitar la tasa de logins.

**¿Por qué SQLite?**
Es la base del proyecto del curso y basta para el MVP. Para producción, el ERS pide
PostgreSQL con cifrado en reposo y respaldos (trabajo futuro).

**¿Qué hace Trivy y qué hace Snyk?**
Trivy revisa la **imagen** (paquetes del sistema operativo Alpine). Snyk revisa las
**dependencias Java** del `pom.xml`.

**¿Qué normativa aplica?**
Ley 19.628 y Ley 21.719 de protección de datos personales: el MVP minimiza los datos,
no los escribe en los logs, controla el acceso por rol y hace eliminación lógica para
la trazabilidad. Ver sección 6 del informe.
