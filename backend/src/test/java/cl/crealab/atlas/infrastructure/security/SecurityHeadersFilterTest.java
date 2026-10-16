package cl.crealab.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** NFR-SEG-5: cabeceras de seguridad (validadas tambien por OWASP ZAP). */
class SecurityHeadersFilterTest {

  @Test
  void agregaCabecerasDeSeguridad() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new SecurityHeadersFilter()
        .doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
    assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
    assertThat(response.getHeader("Content-Security-Policy")).contains("default-src 'self'");
    assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
    assertThat(response.getHeader("Permissions-Policy")).contains("geolocation=()");
  }
}
