package cl.crealab.atlas.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.crealab.atlas.application.ClienteService;
import cl.crealab.atlas.application.dto.ClienteResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** CP-FUN-03 / CP-SEG-01 / CP-SEG-02: contrato HTTP de clientes. */
@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

  @Mock private ClienteService servicio;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    UsuarioAutenticado usuario = new UsuarioAutenticado(1L, "admin.demo", "jti", Datos.AHORA);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ClienteController(servicio))
            .setControllerAdvice(new ApiExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  @AfterEach
  void limpiar() {
    SecurityContextHolder.clearContext();
  }

  private static ClienteResponse respuesta() {
    return new ClienteResponse(
        9L, 1L, "Ferreteria", null, null, null, null, null, null, null, Datos.AHORA);
  }

  @Test
  void listaClientesPaginados() throws Exception {
    when(servicio.listar(1L, 1L, "fer", 0, 10))
        .thenReturn(new PaginaResponse<>(List.of(respuesta()), 0, 10, 1, 1));
    mockMvc
        .perform(get("/api/empresas/1/clientes").param("nombre", "fer"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contenido[0].nombre").value("Ferreteria"))
        .andExpect(jsonPath("$.totalElementos").value(1));
  }

  @Test
  void creaCliente() throws Exception {
    when(servicio.crear(eq(1L), eq(1L), any())).thenReturn(respuesta());
    mockMvc
        .perform(
            post("/api/empresas/1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ferreteria\",\"rut\":\"76123456-0\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(9));
  }

  @Test
  void rechazaScriptEnElNombre() throws Exception {
    mockMvc
        .perform(
            post("/api/empresas/1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"<script>alert(1)</script>\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.startsWith("nombre")));
    verify(servicio, never()).crear(any(), any(), any());
  }

  @Test
  void rechazaInyeccionSqlEnElNombre() throws Exception {
    mockMvc
        .perform(
            post("/api/empresas/1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"x'; DROP TABLE cliente;--\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void editorRecibe403() throws Exception {
    when(servicio.crear(eq(1L), eq(1L), any()))
        .thenThrow(new AccesoDenegadoException("La operacion requiere rol administrador"));
    mockMvc
        .perform(
            post("/api/empresas/1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ferreteria\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.mensaje").value("La operacion requiere rol administrador"));
  }

  @Test
  void eliminaClienteConRespuesta204() throws Exception {
    mockMvc.perform(delete("/api/clientes/9")).andExpect(status().isNoContent());
    verify(servicio).eliminar(1L, 9L);
  }
}
