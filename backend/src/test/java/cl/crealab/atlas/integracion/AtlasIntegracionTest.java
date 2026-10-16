package cl.crealab.atlas.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Pruebas de integracion contra PostgreSQL real (Testcontainers) con las migraciones Flyway y los
 * datos sinteticos de QA. Requiere Docker; sin Docker se omiten.
 *
 * <p>Cubre CP-FUN-02, CP-INT-01, CP-INT-03, CP-INT-04, CP-SEG-01, CP-SEG-03 y CP-SEG-05.
 */
@SpringBootTest(properties = {"atlas.storage.dir=target/test-storage", "atlas.cookie.secure=false"})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AtlasIntegracionTest {

  private static final String CLAVE = "Atlas#Demo2026";

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbc;

  private String login(String usuario) throws Exception {
    MvcResult resultado =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"nombreUsuario\":\"" + usuario + "\",\"contrasena\":\"" + CLAVE + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
    return "Bearer " + json.get("accessToken").asText();
  }

  @Test
  void sinTokenLaApiResponde401() throws Exception {
    mockMvc.perform(get("/api/empresas/1/clientes")).andExpect(status().isUnauthorized());
  }

  @Test
  void editorPuedeLeerPeroNoEscribir() throws Exception {
    String editor = login("editor.demo");
    mockMvc
        .perform(get("/api/empresas/1/clientes").header("Authorization", editor))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/empresas/1/clientes")
                .header("Authorization", editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Cliente Editor\"}"))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(delete("/api/clientes/1").header("Authorization", editor))
        .andExpect(status().isForbidden());
  }

  @Test
  void usuarioDeOtraEmpresaNoVeLosDatos() throws Exception {
    String otro = login("admin.otra");
    mockMvc
        .perform(get("/api/empresas/1/clientes").header("Authorization", otro))
        .andExpect(status().isForbidden());
  }

  @Test
  void datosBancariosQuedanCifradosEnReposo() throws Exception {
    String admin = login("admin.demo");
    MvcResult creado =
        mockMvc
            .perform(
                post("/api/empresas/1/clientes")
                    .header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"nombre\":\"Cliente Cifrado\",\"banco\":\"Banco Estado\","
                            + "\"numeroCuenta\":\"000123456789\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroCuenta").value("000123456789"))
            .andReturn();
    long id = objectMapper.readTree(creado.getResponse().getContentAsString()).get("id").asLong();
    String enBd =
        jdbc.queryForObject("SELECT numero_cuenta FROM cliente WHERE id = ?", String.class, id);
    assertThat(enBd).startsWith("v1:").doesNotContain("000123456789");
    Integer auditados =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM auditoria WHERE accion = 'CREAR_CLIENTE' AND entidad_id = ?",
            Integer.class,
            id);
    assertThat(auditados).isEqualTo(1);
  }

  @Test
  void contrasenasGuardadasConBcryptCosto12() {
    String hash =
        jdbc.queryForObject(
            "SELECT password_hash FROM usuario WHERE nombre_usuario = 'admin.demo'", String.class);
    assertThat(hash).startsWith("$2a$12$");
  }

  @Test
  void logoutRevocaElToken() throws Exception {
    String admin = login("admin.demo");
    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", admin))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/auth/me").header("Authorization", admin))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void subeYDescargaDocumentoYRechazaEjecutable() throws Exception {
    String admin = login("admin.demo");
    byte[] pdf = "%PDF-1.4\n%atlas\n".getBytes(StandardCharsets.US_ASCII);
    mockMvc
        .perform(
            multipart("/api/contratos/1/documentos")
                .file(new MockMultipartFile("archivo", "contrato.pdf", "application/pdf", pdf))
                .header("Authorization", admin))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tipoContenido").value("application/pdf"));
    byte[] descargado =
        mockMvc
            .perform(get("/api/contratos/1/documentos/descarga").header("Authorization", admin))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    assertThat(descargado).isEqualTo(pdf);
    byte[] exe = {0x4D, 0x5A, (byte) 0x90, 0x00};
    mockMvc
        .perform(
            multipart("/api/contratos/1/documentos")
                .file(new MockMultipartFile("archivo", "factura.pdf", "application/pdf", exe))
                .header("Authorization", admin))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void contratosMuestranSuEstadoCalculado() throws Exception {
    String admin = login("admin.demo");
    mockMvc
        .perform(get("/api/clientes/1/contratos").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estado").value("POR_VENCER"))
        .andExpect(jsonPath("$[1].estado").value("ACTIVO"));
  }
}
