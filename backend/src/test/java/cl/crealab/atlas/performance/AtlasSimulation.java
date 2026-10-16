package cl.crealab.atlas.performance;

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
 * CP-PERF-01: carga sobre las operaciones CRUD de lectura con usuarios autenticados.
 *
 * <p>Umbral del ERS (NFR-PERF-1): CRUD simple &lt; 300 ms. Usuarios con -Dvu (por defecto 10 en CI;
 * la prueba completa del informe usa 200 con JMeter).
 */
public class AtlasSimulation extends Simulation {

  private static final int USUARIOS = Integer.getInteger("vu", 10);

  private static final String BASE = System.getProperty("baseUrl", "http://localhost:8080");

  private static final HttpProtocolBuilder HTTP_PROTOCOL =
      http.baseUrl(BASE).acceptHeader("application/json").contentTypeHeader("application/json");

  private static final ScenarioBuilder ESCENARIO =
      scenario("Operacion diaria de la empresa")
          .exec(
              http("login")
                  .post("/api/auth/login")
                  .body(
                      StringBody(
                          "{\"nombreUsuario\":\"admin.demo\",\"contrasena\":\"Atlas#Demo2026\"}"))
                  .check(status().is(200), jsonPath("$.accessToken").saveAs("token")))
          .repeat(5)
          .on(
              exec(http("listar clientes")
                      .get("/api/empresas/1/clientes?pagina=0&tamano=10")
                      .header("Authorization", "Bearer #{token}")
                      .check(status().is(200)))
                  .exec(
                      http("dashboard")
                          .get("/api/empresas/1/dashboard")
                          .header("Authorization", "Bearer #{token}")
                          .check(status().is(200))));

  private static final Assertion SIN_FALLOS = global().failedRequests().count().lt(1L);

  private static final Assertion CRUD_P95 =
      details("listar clientes").responseTime().percentile(95.0).lt(300);

  {
    setUp(ESCENARIO.injectOpen(rampUsers(USUARIOS).during(Duration.ofSeconds(10))))
        .assertions(SIN_FALLOS, CRUD_P95)
        .protocols(HTTP_PROTOCOL);
  }
}
