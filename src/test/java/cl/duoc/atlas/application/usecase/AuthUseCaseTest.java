package cl.duoc.atlas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atlas.domain.exception.CredencialesInvalidasException;
import cl.duoc.atlas.domain.exception.CuentaBloqueadaException;
import cl.duoc.atlas.domain.gateway.TokenGateway;
import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthUseCaseTest {

  private static final Instant AHORA = Instant.parse("2026-10-16T12:00:00Z");
  private static final String HASH = "$2a$12$hashDelAdmin";
  private static final String HASH_FICTICIO = "$2a$12$ficticio";

  @Mock private UsuarioGateway usuarios;
  @Mock private PasswordEncoder encoder;
  @Mock private TokenGateway tokens;

  private MutableClock reloj;
  private AuthUseCase useCase;
  private Usuario admin;
  private Sesion sesion;

  @BeforeEach
  void setUp() {
    reloj = new MutableClock(AHORA);
    when(encoder.encode(anyString())).thenReturn(HASH_FICTICIO);
    useCase = new AuthUseCase(usuarios, encoder, tokens, reloj);
    admin = new Usuario(1L, "admin", HASH, Rol.ADMIN, true);
    sesion = new Sesion("jwt", AHORA.plusSeconds(900), "admin", Rol.ADMIN);
    when(usuarios.findByNombreUsuario("admin")).thenReturn(Optional.of(admin));
    when(encoder.matches("correcta", HASH)).thenReturn(true);
    when(tokens.emitir(admin)).thenReturn(sesion);
  }

  @Test
  void loginCorrectoEmiteToken() {
    assertThat(useCase.login("admin", "correcta")).isSameAs(sesion);
  }

  @Test
  void elNombreDeUsuarioNoDistingueMayusculasNiEspacios() {
    assertThat(useCase.login("  ADMIN ", "correcta")).isSameAs(sesion);
    verify(usuarios).findByNombreUsuario("admin");
  }

  @Test
  void contrasenaIncorrectaFalla() {
    assertThatThrownBy(() -> useCase.login("admin", "mala"))
        .isInstanceOf(CredencialesInvalidasException.class);
    verify(tokens, never()).emitir(any());
  }

  @Test
  void usuarioInexistenteFallaIgualYComparaContraHashFicticio() {
    when(usuarios.findByNombreUsuario("nadie")).thenReturn(Optional.empty());
    when(encoder.matches("x", HASH_FICTICIO)).thenReturn(true);

    assertThatThrownBy(() -> useCase.login("nadie", "x"))
        .isInstanceOf(CredencialesInvalidasException.class)
        .hasMessage("Usuario o contraseña incorrectos");
    verify(encoder).matches("x", HASH_FICTICIO);
  }

  @Test
  void usuarioInactivoNoPuedeEntrar() {
    admin.setActivo(false);

    assertThatThrownBy(() -> useCase.login("admin", "correcta"))
        .isInstanceOf(CredencialesInvalidasException.class);
    verify(tokens, never()).emitir(any());
  }

  @Test
  void bloqueaTrasCincoIntentosFallidos() {
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS; i++) {
      assertThatThrownBy(() -> useCase.login("admin", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }

    assertThatThrownBy(() -> useCase.login("admin", "correcta"))
        .isInstanceOf(CuentaBloqueadaException.class)
        .hasMessageContaining("15 minutos");
    verify(tokens, never()).emitir(any());
    verify(encoder, times(AuthUseCase.MAX_INTENTOS)).matches(eq("mala"), anyString());
  }

  @Test
  void cuatroFallosNoBloquean() {
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS - 1; i++) {
      assertThatThrownBy(() -> useCase.login("admin", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }

    assertThat(useCase.login("admin", "correcta")).isSameAs(sesion);
  }

  @Test
  void unLoginCorrectoReiniciaElContador() {
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS - 1; i++) {
      assertThatThrownBy(() -> useCase.login("admin", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }
    useCase.login("admin", "correcta");
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS - 1; i++) {
      assertThatThrownBy(() -> useCase.login("admin", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }

    assertThat(useCase.login("admin", "correcta")).isSameAs(sesion);
  }

  @Test
  void elBloqueoTerminaTrasQuinceMinutosYReiniciaElConteo() {
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS; i++) {
      assertThatThrownBy(() -> useCase.login("admin", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }
    reloj.avanzar(AuthUseCase.BLOQUEO.minusSeconds(1).getSeconds());
    assertThatThrownBy(() -> useCase.login("admin", "correcta"))
        .isInstanceOf(CuentaBloqueadaException.class);

    reloj.avanzar(1);
    assertThatThrownBy(() -> useCase.login("admin", "mala"))
        .isInstanceOf(CredencialesInvalidasException.class);
    assertThat(useCase.login("admin", "correcta")).isSameAs(sesion);
  }

  @Test
  void elBloqueoEsPorUsuario() {
    for (int i = 0; i < AuthUseCase.MAX_INTENTOS; i++) {
      assertThatThrownBy(() -> useCase.login("otro", "mala"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }

    assertThat(useCase.login("admin", "correcta")).isSameAs(sesion);
  }

  @Test
  void nombreConSaltosDeLineaNoRompeElLogin() {
    when(usuarios.findByNombreUsuario(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.login("ataque\nFALSO", "x"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }

  @Test
  void logoutRevocaElToken() {
    useCase.logout("jwt", "admin");

    verify(tokens).revocar("jwt");
  }

  /** Reloj que se puede adelantar para probar la expiracion del bloqueo. */
  private static final class MutableClock extends Clock {

    private Instant instante;

    MutableClock(Instant inicio) {
      this.instante = inicio;
    }

    void avanzar(long segundos) {
      instante = instante.plusSeconds(segundos);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instante;
    }
  }
}
