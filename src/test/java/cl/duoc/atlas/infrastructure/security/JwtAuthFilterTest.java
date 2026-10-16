package cl.duoc.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {

  private final JwtService jwtService = mock(JwtService.class);
  private final JwtAuthFilter filtro = new JwtAuthFilter(jwtService);
  private final FilterChain cadena = mock(FilterChain.class);

  @AfterEach
  void limpiar() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void tokenValidoAutenticaConElRol() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clientes");
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader("Authorization", "Bearer abc");
    when(jwtService.validar("abc"))
        .thenReturn(Optional.of(new TokenValido("editor", "EDITOR", "j1", Instant.now())));

    filtro.doFilterInternal(request, response, cadena);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getName()).isEqualTo("editor");
    assertThat(auth.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_EDITOR");
    verify(cadena).doFilter(request, response);
  }

  @Test
  void tokenInvalidoDejaLaPeticionAnonima() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader("Authorization", "Bearer malo");
    when(jwtService.validar("malo")).thenReturn(Optional.empty());

    filtro.doFilterInternal(request, response, cadena);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(cadena).doFilter(request, response);
  }

  @Test
  void sinHeaderOConOtroEsquemaNoValida() throws Exception {
    MockHttpServletRequest sinHeader = new MockHttpServletRequest();
    MockHttpServletRequest basic = new MockHttpServletRequest();
    basic.addHeader("Authorization", "Basic YWRtaW46eA==");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filtro.doFilterInternal(sinHeader, response, cadena);
    filtro.doFilterInternal(basic, response, cadena);

    verifyNoInteractions(jwtService);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
