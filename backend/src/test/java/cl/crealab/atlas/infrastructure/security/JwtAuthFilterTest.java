package cl.crealab.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.infrastructure.persistence.TokenRevocadoRepository;
import cl.crealab.atlas.soporte.Datos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

/** RF-1.2: un token revocado por logout ya no autentica. */
class JwtAuthFilterTest {

  private final JwtService jwt = new JwtService(JwtServiceTest.SECRETO, 15, 7, Datos.reloj());
  private final TokenRevocadoRepository revocados = mock(TokenRevocadoRepository.class);
  private final JwtAuthFilter filtro = new JwtAuthFilter(jwt, revocados);

  @AfterEach
  void limpiar() {
    SecurityContextHolder.clearContext();
  }

  private MockHttpServletRequest conToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }

  @Test
  void autenticaConTokenValido() throws Exception {
    TokenEmitido token = jwt.emitir(1L, "admin", JwtService.TIPO_ACCESO);
    when(revocados.existsById(token.jti())).thenReturn(false);
    filtro.doFilter(conToken(token.token()), new MockHttpServletResponse(), new MockFilterChain());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isInstanceOf(UsuarioAutenticado.class);
  }

  @Test
  void noAutenticaConTokenRevocado() throws Exception {
    TokenEmitido token = jwt.emitir(1L, "admin", JwtService.TIPO_ACCESO);
    when(revocados.existsById(token.jti())).thenReturn(true);
    filtro.doFilter(conToken(token.token()), new MockHttpServletResponse(), new MockFilterChain());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void ignoraPeticionSinCabecera() throws Exception {
    MockFilterChain cadena = new MockFilterChain();
    filtro.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), cadena);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(cadena.getRequest()).isNotNull();
  }
}
