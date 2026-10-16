package cl.duoc.atlas.infrastructure.entrypoints.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.atlas.application.usecase.AuthUseCase;
import cl.duoc.atlas.domain.exception.CredencialesInvalidasException;
import cl.duoc.atlas.domain.exception.CuentaBloqueadaException;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Sesion;
import java.time.Instant;
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
class AuthControllerTest {

  private static final String LOGIN = "{\"nombreUsuario\":\"admin\",\"contrasena\":\"x\"}";

  @Mock private AuthUseCase useCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(useCase))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  @Test
  void loginCorrectoDevuelveElToken() throws Exception {
    when(useCase.login("admin", "x"))
        .thenReturn(new Sesion("jwt", Instant.parse("2026-10-16T12:15:00Z"), "admin", Rol.ADMIN));

    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt"))
        .andExpect(jsonPath("$.tipo").value("Bearer"))
        .andExpect(jsonPath("$.usuario").value("admin"))
        .andExpect(jsonPath("$.rol").value("ADMIN"));
  }

  @Test
  void credencialesInvalidasDevuelve401() throws Exception {
    when(useCase.login("admin", "x")).thenThrow(new CredencialesInvalidasException());

    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
  }

  @Test
  void cuentaBloqueadaDevuelve429() throws Exception {
    when(useCase.login("admin", "x")).thenThrow(new CuentaBloqueadaException(15));

    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
        .andExpect(status().isTooManyRequests());
  }

  @Test
  void loginSinDatosDevuelve400() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.nombreUsuario").exists())
        .andExpect(jsonPath("$.details.contrasena").exists());
  }

  @Test
  void logoutRevocaElTokenDelHeader() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .header("Authorization", "Bearer jwt-actual")
                .principal(new UsernamePasswordAuthenticationToken("admin", null)))
        .andExpect(status().isNoContent());

    verify(useCase).logout("jwt-actual", "admin");
  }

  @Test
  void metodoNoPermitidoDevuelve405() throws Exception {
    mockMvc.perform(get("/api/v1/auth/login")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void errorInesperadoDevuelve500SinDetallesInternos() throws Exception {
    when(useCase.login("admin", "x")).thenThrow(new IllegalStateException("detalle interno"));

    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.message")
                .value("Ocurrió un error inesperado. Intente nuevamente más tarde"));
  }
}
