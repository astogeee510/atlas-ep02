package cl.crealab.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.soporte.Datos;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** CP-FUN-02 / NFR-SEG-9: emision y validacion de JWT. */
class JwtServiceTest {

  static final String SECRETO = "ZGV2LXNvbG8tcGFyYS1kZXNhcnJvbGxvLWNhbWJpYXItZW4tcWEtMzJieXRlcyEh";

  private final JwtService servicio = new JwtService(SECRETO, 15, 7, Datos.reloj());

  @Test
  void tokenDeAccesoValido() {
    TokenEmitido token = servicio.emitir(10L, "ana", JwtService.TIPO_ACCESO);
    assertThat(token.expiraEn()).isEqualTo(Datos.AHORA.plus(Duration.ofMinutes(15)));
    UsuarioAutenticado usuario =
        servicio.validar(token.token(), JwtService.TIPO_ACCESO).orElseThrow();
    assertThat(usuario.id()).isEqualTo(10L);
    assertThat(usuario.nombreUsuario()).isEqualTo("ana");
    assertThat(usuario.jti()).isEqualTo(token.jti());
  }

  @Test
  void refrescoDuraSieteDias() {
    TokenEmitido token = servicio.emitir(10L, "ana", JwtService.TIPO_REFRESCO);
    assertThat(token.expiraEn()).isEqualTo(Datos.AHORA.plus(Duration.ofDays(7)));
    assertThat(servicio.getDuracionRefresco()).isEqualTo(Duration.ofDays(7));
  }

  @Test
  void tokenDeRefrescoNoSirveComoAcceso() {
    TokenEmitido token = servicio.emitir(10L, "ana", JwtService.TIPO_REFRESCO);
    assertThat(servicio.validar(token.token(), JwtService.TIPO_ACCESO)).isEmpty();
  }

  @Test
  void tokenManipuladoEsRechazado() {
    String token = servicio.emitir(10L, "ana", JwtService.TIPO_ACCESO).token();
    int punto = token.lastIndexOf('.');
    char original = token.charAt(punto + 1);
    String manipulado =
        token.substring(0, punto + 1) + (original == 'A' ? 'B' : 'A') + token.substring(punto + 2);
    assertThat(servicio.validar(manipulado, JwtService.TIPO_ACCESO)).isEmpty();
  }

  @Test
  void tokenExpiradoEsRechazado() {
    String token = servicio.emitir(10L, "ana", JwtService.TIPO_ACCESO).token();
    JwtService masTarde =
        new JwtService(
            SECRETO, 15, 7, Clock.fixed(Datos.AHORA.plus(Duration.ofMinutes(16)), Datos.ZONA));
    assertThat(masTarde.validar(token, JwtService.TIPO_ACCESO)).isEmpty();
  }

  @Test
  void tokenVacioOBasuraEsRechazado() {
    assertThat(servicio.validar(null, JwtService.TIPO_ACCESO)).isEmpty();
    assertThat(servicio.validar("", JwtService.TIPO_ACCESO)).isEmpty();
    assertThat(servicio.validar("no.es.jwt", JwtService.TIPO_ACCESO)).isEmpty();
  }
}
