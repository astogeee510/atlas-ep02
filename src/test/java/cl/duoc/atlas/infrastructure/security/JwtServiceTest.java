package cl.duoc.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import io.jsonwebtoken.security.WeakKeyException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/** CP-AUT-01 / CP-AUT-04: emision, validacion, expiracion y revocacion de JWT. */
class JwtServiceTest {

  private static final String SECRETO =
      Base64.getEncoder().encodeToString("clave-de-pruebas-de-32-bytes-ok!".getBytes());
  private static final Instant AHORA = Instant.parse("2026-10-16T12:00:00Z");

  private final Usuario admin = new Usuario(1L, "admin", "h", Rol.ADMIN, true);

  private JwtService servicio(Instant instante) {
    return new JwtService(SECRETO, 15, Clock.fixed(instante, ZoneOffset.UTC));
  }

  @Test
  void emiteUnTokenValidoConUsuarioYRol() {
    JwtService jwt = servicio(AHORA);

    Sesion sesion = jwt.emitir(admin);

    assertThat(sesion.expiraEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(15)));
    assertThat(sesion.nombreUsuario()).isEqualTo("admin");
    assertThat(sesion.rol()).isEqualTo(Rol.ADMIN);
    TokenValido valido = jwt.validar(sesion.token()).orElseThrow();
    assertThat(valido.nombreUsuario()).isEqualTo("admin");
    assertThat(valido.rol()).isEqualTo("ADMIN");
    assertThat(valido.jti()).isNotBlank();
    assertThat(valido.expiraEn()).isEqualTo(sesion.expiraEn());
    assertThat(jwt.getDuracion()).isEqualTo(Duration.ofMinutes(15));
  }

  @Test
  void cadaTokenTieneUnJtiDistinto() {
    JwtService jwt = servicio(AHORA);

    String a = jwt.validar(jwt.emitir(admin).token()).orElseThrow().jti();
    String b = jwt.validar(jwt.emitir(admin).token()).orElseThrow().jti();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void rechazaTokenExpirado() {
    String token = servicio(AHORA).emitir(admin).token();

    assertThat(servicio(AHORA.plus(Duration.ofMinutes(16))).validar(token)).isEmpty();
  }

  @Test
  void rechazaTokenAlteradoOFirmadoConOtraClave() {
    JwtService jwt = servicio(AHORA);
    String token = jwt.emitir(admin).token();
    String alterado = token.substring(0, token.length() - 2) + "xx";
    JwtService otraClave = new JwtService("", 15, Clock.fixed(AHORA, ZoneOffset.UTC));

    assertThat(jwt.validar(alterado)).isEmpty();
    assertThat(otraClave.validar(token)).isEmpty();
    assertThat(jwt.validar("no-es-un-jwt")).isEmpty();
    assertThat(jwt.validar("")).isEmpty();
    assertThat(jwt.validar(null)).isEmpty();
  }

  @Test
  void sinSecretoGeneraUnaClaveAleatoriaFuncional() {
    JwtService jwt = new JwtService(null, 15, Clock.fixed(AHORA, ZoneOffset.UTC));

    assertThat(jwt.validar(jwt.emitir(admin).token())).isPresent();
  }

  @Test
  void rechazaSecretosDebiles() {
    String corto = Base64.getEncoder().encodeToString("corto".getBytes());

    assertThatThrownBy(() -> new JwtService(corto, 15, Clock.systemUTC()))
        .isInstanceOf(WeakKeyException.class);
  }

  @Test
  void revocarInvalidaElTokenYNoAfectaAOtros() {
    JwtService jwt = servicio(AHORA);
    String revocado = jwt.emitir(admin).token();
    String vigente = jwt.emitir(admin).token();
    String jti = jwt.validar(revocado).orElseThrow().jti();

    jwt.revocar(revocado);

    assertThat(jwt.validar(revocado)).isEmpty();
    assertThat(jwt.estaRevocado(jti)).isTrue();
    assertThat(jwt.validar(vigente)).isPresent();
  }

  @Test
  void revocarUnTokenInvalidoNoHaceNada() {
    JwtService jwt = servicio(AHORA);

    jwt.revocar("basura");

    assertThat(jwt.estaRevocado("basura")).isFalse();
  }
}
