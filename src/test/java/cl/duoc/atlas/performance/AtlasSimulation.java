package cl.duoc.atlas.performance;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

/**
 * CP-PERF-01: carga sobre las consultas de clientes y contratos con usuarios autenticados.
 *
 * <p>Umbral del ERS (NFR-PERF-1): operaciones CRUD simples bajo 300 ms (p95) y sin errores.
 * Usuarios con -Dvu (10 por defecto en CI).
 */
public class AtlasSimulation extends Simulation {

  private static final int USUARIOS = Integer.getInteger("vu", 10);
  private static final String BASE = System.getProperty("baseUrl", "http://localhost:8080");
  private static final String PASSWORD = System.getProperty("password", "Editor#Atlas2026");

  private static final HttpProtocolBuilder HTTP_PROTOCOL =
      http.baseUrl(BASE).acceptHeader("application/json").contentTypeHeader("application/json");

  private static final ScenarioBuilder ESCENARIO =
      scenario("Consulta diaria de clientes y contratos")
          .exec(
              http("login")
                  .post("/api/v1/auth/login")
                  .body(
                      StringBody(
                          "{\"nombreUsuario\":\"editor\",\"contrasena\":\"" + PASSWORD + "\"}"))
                  .check(status().is(200), jsonPath("$.accessToken").saveAs("token")))
          .repeat(5)
          .on(
              exec(http("listar clientes")
                      .get("/api/v1/clientes?pagina=0&tamano=10")
                      .header("Authorization", "Bearer #{token}")
                      .check(status().is(200), jsonPath("$.contenido[0].id").saveAs("clienteId")))
                  .exec(
                      http("contratos del cliente")
                          .get("/api/v1/clientes/#{clienteId}/contratos")
                          .header("Authorization", "Bearer #{token}")
                          .check(status().is(200))));

  private static final Assertion SIN_FALLOS = global().failedRequests().count().lt(1L);

  private static final Assertion CLIENTES_P95 =
      details("listar clientes").responseTime().percentile(95.0).lt(300);

  private static final Assertion CONTRATOS_P95 =
      details("contratos del cliente").responseTime().percentile(95.0).lt(300);

  {
    setUp(ESCENARIO.injectOpen(rampUsers(USUARIOS).during(Duration.ofSeconds(10))))
        .assertions(SIN_FALLOS, CLIENTES_P95, CONTRATOS_P95)
        .protocols(HTTP_PROTOCOL);
  }
}
