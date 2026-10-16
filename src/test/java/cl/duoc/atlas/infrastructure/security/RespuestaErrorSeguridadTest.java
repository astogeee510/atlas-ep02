package cl.duoc.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class RespuestaErrorSeguridadTest {

  private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
  private final RespuestaErrorSeguridad respuesta = new RespuestaErrorSeguridad(mapper);

  @Test
  void sinAutenticacionResponde401EnJson() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clientes");
    MockHttpServletResponse response = new MockHttpServletResponse();

    respuesta.commence(request, response, new InsufficientAuthenticationException("x"));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).startsWith("application/json");
    assertThat(response.getContentAsString())
        .contains("\"status\":401")
        .contains("Debe iniciar sesión")
        .contains("/api/v1/clientes");
  }

  @Test
  void sinPermisoResponde403EnJson() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/clientes");
    MockHttpServletResponse response = new MockHttpServletResponse();

    respuesta.handle(request, response, new AccessDeniedException("x"));

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentAsString())
        .contains("\"status\":403")
        .contains("Su rol no tiene permiso");
  }
}
