package cl.duoc.atlas.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** CP-SEG-03: cabeceras de seguridad en todas las respuestas (NFR-SEG-5). */
class SecurityHeadersFilterTest {

  private final SecurityHeadersFilter filtro = new SecurityHeadersFilter();

  @Test
  void agregaLasCabecerasDeSeguridadEnCadaRespuesta() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain cadena = (req, res) -> {};

    filtro.doFilterInternal(request, response, cadena);

    assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
    assertThat(response.getHeader("Content-Security-Policy"))
        .isEqualTo(SecurityHeadersFilter.CSP)
        .startsWith("default-src 'self'")
        .contains("frame-ancestors 'none'");
    assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
    assertThat(response.getHeader("Permissions-Policy")).contains("geolocation=()");
    assertThat(response.getHeader("Cross-Origin-Opener-Policy")).isEqualTo("same-origin");
    assertThat(response.getHeader("Cross-Origin-Resource-Policy")).isEqualTo("same-origin");
  }
}
