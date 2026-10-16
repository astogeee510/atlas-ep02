package cl.duoc.atlas.infrastructure.entrypoints.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.atlas.application.usecase.ContratoUseCase;
import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.EstadoContrato;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ContratoControllerTest {

  private static final String JSON =
      "{\"titulo\":\"Suministro\",\"descripcion\":\"d\","
          + "\"fechaInicio\":\"2026-01-01\",\"fechaTermino\":\"2026-12-31\"}";

  @Mock private ContratoUseCase useCase;

  private MockMvc mockMvc;
  private final Principal admin = new UsernamePasswordAuthenticationToken("admin", null);
  private final Contrato contrato =
      new Contrato(5L, 1L, "Suministro", "d", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ContratoController(useCase))
            .setControllerAdvice(new ApiExceptionHandler())
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(
                    JsonMapper.builder()
                        .findAndAddModules()
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .build()))
            .build();
  }

  @Test
  void listarIncluyeElEstadoCalculado() throws Exception {
    when(useCase.listarPorCliente(1L)).thenReturn(List.of(contrato));
    when(useCase.estado(contrato)).thenReturn(EstadoContrato.POR_VENCER);

    mockMvc
        .perform(get("/api/v1/clientes/1/contratos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(5))
        .andExpect(jsonPath("$[0].clienteId").value(1))
        .andExpect(jsonPath("$[0].fechaTermino").value("2026-12-31"))
        .andExpect(jsonPath("$[0].estado").value("POR_VENCER"));
  }

  @Test
  void listarDeClienteInexistenteDevuelve404() throws Exception {
    when(useCase.listarPorCliente(9L)).thenThrow(new RecursoNoEncontradoException("cliente", 9L));

    mockMvc.perform(get("/api/v1/clientes/9/contratos")).andExpect(status().isNotFound());
  }

  @Test
  void crearDevuelve201() throws Exception {
    when(useCase.crear(eq(1L), any(Contrato.class), eq("admin"))).thenReturn(contrato);
    when(useCase.estado(contrato)).thenReturn(EstadoContrato.ACTIVO);

    mockMvc
        .perform(
            post("/api/v1/clientes/1/contratos")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estado").value("ACTIVO"));
  }

  @Test
  void crearSinFechasDevuelve400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clientes/1/contratos")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.titulo").exists())
        .andExpect(jsonPath("$.details.fechaInicio").exists())
        .andExpect(jsonPath("$.details.fechaTermino").exists());
  }

  @Test
  void crearConFechaMalFormadaDevuelve400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clientes/1/contratos")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"titulo\":\"T\",\"fechaInicio\":\"31-12-2026\","
                        + "\"fechaTermino\":\"2026-12-31\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void crearConFechasIncoherentesDevuelve400() throws Exception {
    when(useCase.crear(eq(1L), any(Contrato.class), eq("admin")))
        .thenThrow(new DatosInvalidosException("fechas"));

    mockMvc
        .perform(
            post("/api/v1/clientes/1/contratos")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("fechas"));
  }

  @Test
  void obtenerActualizarYEliminar() throws Exception {
    when(useCase.obtener(5L)).thenReturn(contrato);
    when(useCase.actualizar(eq(5L), any(Contrato.class), eq("admin"))).thenReturn(contrato);
    when(useCase.estado(contrato)).thenReturn(EstadoContrato.EXPIRADO);

    mockMvc
        .perform(get("/api/v1/contratos/5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("EXPIRADO"));
    mockMvc
        .perform(
            put("/api/v1/contratos/5")
                .principal(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isOk());
    mockMvc
        .perform(delete("/api/v1/contratos/5").principal(admin))
        .andExpect(status().isNoContent());
    verify(useCase).eliminar(5L, "admin");
  }
}
