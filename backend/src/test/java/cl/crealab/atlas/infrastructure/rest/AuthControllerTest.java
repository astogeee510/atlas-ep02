package cl.crealab.atlas.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.crealab.atlas.application.AuthService;
import cl.crealab.atlas.application.dto.ResultadoLogin;
import cl.crealab.atlas.application.dto.SesionResponse;
import cl.crealab.atlas.application.dto.UsuarioResponse;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.infrastructure.security.TokenEmitido;
import java.time.Instant;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** CP-FUN-01 / CP-FUN-02: contrato HTTP de autenticacion. */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(authService, true))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  @Test
  void loginEntregaJwtYCookieHttpOnly() throws Exception {
    SesionResponse sesion =
        new SesionResponse("jwt", "Bearer", 900, new UsuarioResponse(1L, "a", "a@x"), List.of());
    TokenEmitido refresco = new TokenEmitido("ref", "j", Instant.now().plusSeconds(3600));
    when(authService.login(any(), anyString())).thenReturn(new ResultadoLogin(sesion, refresco));
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"admin.demo\",\"contrasena\":\"Atlas#Demo2026\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt"))
        .andExpect(header().string("Set-Cookie", Matchers.containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", Matchers.containsString("SameSite=Strict")))
        .andExpect(header().string("Set-Cookie", Matchers.containsString("Secure")));
  }

  @Test
  void credencialesInvalidasDevuelven401() throws Exception {
    when(authService.login(any(), anyString()))
        .thenThrow(new CredencialesInvalidasException("Usuario o contrasena incorrectos"));
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"admin.demo\",\"contrasena\":\"mala\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void registroSinConsentimientoEsRechazado() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nombreUsuario\":\"nuevo\",\"correo\":\"n@x.cl\","
                        + "\"contrasena\":\"Atlas#Demo2026\",\"consentimiento\":false}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refrescoSinCookieDevuelve401() throws Exception {
    mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
  }

  @Test
  void refrescoConCookie() throws Exception {
    when(authService.refrescar("ref"))
        .thenReturn(
            new SesionResponse(
                "nuevo", "Bearer", 900, new UsuarioResponse(1L, "a", "a@x"), List.of()));
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie(AuthController.COOKIE_REFRESCO, "ref")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("nuevo"));
  }
}
