package cl.duoc.atlas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.atlas.infrastructure.adapters.jpa.UsuarioJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integracion con la aplicacion completa (Spring Security + JWT + SQLite real).
 *
 * <p>Cubren CP-AUT-01..05 (login, credenciales invalidas, bloqueo, logout y token alterado),
 * CP-SEG-01 (hash bcrypt en la base de datos), CP-ROL-01..02 (EDITOR solo lectura, ADMIN escritura)
 * y CP-SEG-03 (cabeceras de seguridad).
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:target/atlas-it.db",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "atlas.datos-demo=true",
      "atlas.usuarios.admin-password=Admin#Prueba1",
      "atlas.usuarios.editor-password=Editor#Prueba1"
    })
@AutoConfigureMockMvc
class SeguridadIntegracionTest {

  private static final String CLIENTE =
      "{\"rut\":\"%s\",\"nombre\":\"Cliente Integración\",\"direccion\":\"Calle 1\","
          + "\"email\":\"it@atlas.cl\",\"telefono\":\"+56912345678\"}";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UsuarioJpaRepository usuarios;

  private String login(String usuario, String contrasena) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"nombreUsuario\":\""
                            + usuario
                            + "\",\"contrasena\":\""
                            + contrasena
                            + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).get("accessToken").asText();
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  @Test
  void lasContrasenasSeGuardanComoHashBcryptCosto12() {
    String hash = usuarios.findByNombreUsuario("admin").orElseThrow().getHashContrasena();

    assertThat(hash).startsWith("$2a$12$").hasSize(60).doesNotContain("Admin#Prueba1");
  }

  @Test
  void sinTokenLaApiResponde401ConCabecerasDeSeguridad() throws Exception {
    mockMvc
        .perform(get("/api/v1/clientes"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(header().string("X-Frame-Options", "DENY"))
        .andExpect(header().exists("Content-Security-Policy"));
  }

  @Test
  void laDocumentacionYLaHomeSonPublicas() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    mockMvc.perform(get("/openapi/atlas-openapi.yaml")).andExpect(status().isOk());
    mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
  }

  @Test
  void credencialesInvalidasResponden401SinRevelarElMotivo() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"admin\",\"contrasena\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
  }

  @Test
  void bloqueaElUsuarioTrasCincoIntentosFallidos() throws Exception {
    String body = "{\"nombreUsuario\":\"atacante\",\"contrasena\":\"x\"}";
    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isUnauthorized());
    }
    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isTooManyRequests());
  }

  @Test
  void tokenAlteradoEsRechazado() throws Exception {
    String token = login("editor", "Editor#Prueba1");
    String alterado = token.substring(0, token.length() - 3) + "abc";

    mockMvc
        .perform(get("/api/v1/clientes").header("Authorization", bearer(alterado)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void editorSoloPuedeLeer() throws Exception {
    String token = login("editor", "Editor#Prueba1");

    mockMvc
        .perform(get("/api/v1/clientes").header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").isNumber());
    mockMvc
        .perform(
            post("/api/v1/clientes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.formatted("11.111.111-1")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Su rol no tiene permiso para esta operación"));
    mockMvc
        .perform(
            delete("/api/v1/clientes/1")
                .param("confirmar", "true")
                .header("Authorization", bearer(token)))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminGestionaClientesYContratosDeExtremoAExtremo() throws Exception {
    String token = login("admin", "Admin#Prueba1");

    String creado =
        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CLIENTE.formatted("15.000.000-9")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rut").value("15000000-9"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long clienteId = objectMapper.readTree(creado).get("id").asLong();

    mockMvc
        .perform(
            post("/api/v1/clientes")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.formatted("15000000-9")))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            post("/api/v1/clientes/" + clienteId + "/contratos")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"titulo\":\"Vencido\",\"fechaInicio\":\"2020-01-01\","
                        + "\"fechaTermino\":\"2020-12-31\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estado").value("EXPIRADO"));

    mockMvc
        .perform(
            put("/api/v1/clientes/" + clienteId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.formatted("15000000-9").replace("Integración", "Editado")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Cliente Editado"));

    mockMvc
        .perform(
            get("/api/v1/clientes")
                .param("nombre", "editado")
                .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contenido[0].id").value(clienteId));

    mockMvc
        .perform(delete("/api/v1/clientes/" + clienteId).header("Authorization", bearer(token)))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            delete("/api/v1/clientes/" + clienteId)
                .param("confirmar", "true")
                .header("Authorization", bearer(token)))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/v1/clientes/" + clienteId).header("Authorization", bearer(token)))
        .andExpect(status().isNotFound());
  }

  @Test
  void losDatosDemoMuestranLosTresEstados() throws Exception {
    String token = login("editor", "Editor#Prueba1");

    String body =
        mockMvc
            .perform(
                get("/api/v1/clientes")
                    .param("nombre", "Ferretería")
                    .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode cliente = objectMapper.readTree(body).get("contenido").get(0);

    mockMvc
        .perform(
            get("/api/v1/clientes/" + cliente.get("id").asLong() + "/contratos")
                .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estado").value("POR_VENCER"))
        .andExpect(jsonPath("$[1].estado").value("ACTIVO"));
  }

  @Test
  void logoutRevocaElToken() throws Exception {
    String token = login("admin", "Admin#Prueba1");

    mockMvc
        .perform(post("/api/v1/auth/logout").header("Authorization", bearer(token)))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/v1/clientes").header("Authorization", bearer(token)))
        .andExpect(status().isUnauthorized());
    assertThat(token).isNotBlank();
  }
}
