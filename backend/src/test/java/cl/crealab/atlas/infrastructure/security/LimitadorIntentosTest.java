package cl.crealab.atlas.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.crealab.atlas.domain.exception.DemasiadosIntentosException;
import cl.crealab.atlas.soporte.Datos;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** Proteccion contra fuerza bruta en el login. */
class LimitadorIntentosTest {

  /** Reloj que se puede adelantar. */
  static final class RelojMovil extends Clock {
    private Instant ahora = Datos.AHORA;

    void avanzar(Duration d) {
      ahora = ahora.plus(d);
    }

    @Override
    public ZoneId getZone() {
      return Datos.ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return ahora;
    }
  }

  @Test
  void bloqueaTrasCincoFallosYLiberaAlPasarLaVentana() {
    RelojMovil reloj = new RelojMovil();
    LimitadorIntentos limitador = new LimitadorIntentos(5, 60, reloj);
    for (int i = 0; i < 4; i++) {
      limitador.registrarFallo("ana|ip");
    }
    assertThatCode(() -> limitador.verificar("ana|ip")).doesNotThrowAnyException();
    limitador.registrarFallo("ana|ip");
    assertThatThrownBy(() -> limitador.verificar("ana|ip"))
        .isInstanceOf(DemasiadosIntentosException.class);
    reloj.avanzar(Duration.ofSeconds(61));
    assertThatCode(() -> limitador.verificar("ana|ip")).doesNotThrowAnyException();
  }

  @Test
  void loginExitosoLimpiaLosFallos() {
    LimitadorIntentos limitador = new LimitadorIntentos(1, 60, Datos.reloj());
    limitador.registrarFallo("ana|ip");
    limitador.limpiar("ana|ip");
    assertThatCode(() -> limitador.verificar("ana|ip")).doesNotThrowAnyException();
  }

  @Test
  void claveSinFallosNoSeBloquea() {
    LimitadorIntentos limitador = new LimitadorIntentos(1, 60, Datos.reloj());
    assertThatCode(() -> limitador.verificar("otro|ip")).doesNotThrowAnyException();
  }
}
