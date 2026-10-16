package cl.duoc.atlas.infrastructure.entrypoints.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.atlas.application.usecase.ClienteUseCase;
import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoDuplicadoException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Pagina;
import java.security.Principal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

  private static final String JSON_VALIDO =
      "{\"rut\":\"12.345.678-5\",\"nombre\":\"Ferretería Los Andes\",\"descripcion\":\"x\","
          + "\"direccion\":\"Av. Matta 500\",\"email\":\"a@b.cl\",\"telefono\":\"+56912345678\"}";

  @Mock private ClienteUseCase useCase;

  private MockMvc mockMvc;
  private final Principal admin = new UsernamePasswordAuthenticationToken("admin", null);

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ClienteController(useCase))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  private Cliente cliente() {
    return new Cliente(1L, "12345678-5", "Ferretería", "x", "Av", "a@b.cl", "12345678", true);
  }

  @Test
  void listarDevuelvePagina() throws Exception {
    when(useCase.listar("fe", 0, 10)).thenReturn(new Pagina<>(List.of(cliente()), 0, 10, 1));

    mockMvc
        .perform(get("/api/v1/clientes").param("nombre", "fe"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contenido[0].rut").value("12345678-5"))
        .andExpect(jsonPath("$.totalElementos").value(1))
        .andExpect(jsonPath("$.totalPaginas").value(1));
  }

  @Test
  void listarConPaginacionInvalidaDevuelve400() throws Exception {
    when(useCase.listar("", 0, 500)).thenThrow(new DatosInvalidosException("tamaño"));

    mockMvc
        .perform(get("/api/v1/clientes").param("tamano", "500"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("tamaño"));
  }

  @Test
  void obtenerDevuelve200() throws Exception {
    when(useCase.obtener(1L)).thenReturn(cliente());

    mockMvc
        .perform(get("/api/v1/clientes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Ferretería"));
  }

  @Test
  void obtenerDevuelve404() throws Exception {
    when(useCase.obtener(9L)).thenThrow(new RecursoNoEncontradoException("cliente", 9L));

    mockMvc
        .perform(get("/api/v1/clientes/9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("No existe un cliente con id 9"));
  }

  @Test
  void idNoNumericoDevuelve400() throws Exception {
    mockMvc.perform(get("/api/v1/clientes/abc")).andExpect(status().isBadRequest());
  }

  @Test
  void crearDevuelve201() throws Exception {
    when(useCase.crear(any(Cliente.class), eq("admin"))).thenReturn(cliente());

    mockMvc
        .perform(
            post("/api/v1/clientes")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void crearRechazaScriptsYFormatosInvalidos() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clientes")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"rut\":\"abc\",\"nombre\":\"<script>alert(1)</script>\","
                        + "\"direccion\":\"<b>x</b>\",\"email\":\"no-email\",\"telefono\":\"12\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.rut").exists())
        .andExpect(jsonPath("$.details.nombre").exists())
        .andExpect(jsonPath("$.details.direccion").exists())
        .andExpect(jsonPath("$.details.email").exists())
        .andExpect(jsonPath("$.details.telefono").exists());
  }

  @Test
  void crearConJsonMalFormadoDevuelve400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clientes")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{no es json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void crearDuplicadoDevuelve409() throws Exception {
    when(useCase.crear(any(Cliente.class), eq("admin")))
        .thenThrow(new RecursoDuplicadoException("dup"));

    mockMvc
        .perform(
            post("/api/v1/clientes")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
        .andExpect(status().isConflict());
  }

  @Test
  void actualizarDevuelve200() throws Exception {
    when(useCase.actualizar(eq(1L), any(Cliente.class), eq("admin"))).thenReturn(cliente());

    mockMvc
        .perform(
            put("/api/v1/clientes/1")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
        .andExpect(status().isOk());
  }

  @Test
  void eliminarConConfirmacionDevuelve204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/clientes/1").param("confirmar", "true").principal(admin))
        .andExpect(status().isNoContent());

    verify(useCase).eliminar(1L, true, "admin");
  }

  @Test
  void eliminarSinConfirmacionDevuelve400() throws Exception {
    doThrow(new DatosInvalidosException("confirme")).when(useCase).eliminar(1L, false, "admin");

    mockMvc
        .perform(delete("/api/v1/clientes/1").principal(admin))
        .andExpect(status().isBadRequest());
  }
}
