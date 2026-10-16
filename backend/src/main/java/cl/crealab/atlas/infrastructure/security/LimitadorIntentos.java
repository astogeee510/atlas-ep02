package cl.crealab.atlas.infrastructure.security;

import cl.crealab.atlas.domain.exception.DemasiadosIntentosException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Proteccion contra fuerza bruta: bloquea temporalmente una combinacion usuario+IP tras N intentos
 * de login fallidos dentro de una ventana de tiempo.
 */
@Component
public class LimitadorIntentos {

  private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
  private final int maxIntentos;
  private final Duration ventana;
  private final Clock clock;

  public LimitadorIntentos(
      @Value("${atlas.login.max-intentos-fallidos}") int maxIntentos,
      @Value("${atlas.login.ventana-segundos}") long ventanaSegundos,
      Clock clock) {
    this.maxIntentos = maxIntentos;
    this.ventana = Duration.ofSeconds(ventanaSegundos);
    this.clock = clock;
  }

  /**
   * Lanza {@link DemasiadosIntentosException} si la clave supero el limite.
   *
   * @param clave usuario + IP
   */
  public void verificar(String clave) {
    Deque<Instant> registro = fallos.get(clave);
    if (registro == null) {
      return;
    }
    Instant limite = clock.instant().minus(ventana);
    registro.removeIf(instante -> instante.isBefore(limite));
    if (registro.size() >= maxIntentos) {
      throw new DemasiadosIntentosException(
          "Demasiados intentos fallidos. Espere un minuto e intente nuevamente.");
    }
  }

  /**
   * Registra un intento fallido.
   *
   * @param clave usuario + IP
   */
  public void registrarFallo(String clave) {
    fallos.computeIfAbsent(clave, k -> new ConcurrentLinkedDeque<>()).addLast(clock.instant());
  }

  /**
   * Limpia los fallos tras un login exitoso.
   *
   * @param clave usuario + IP
   */
  public void limpiar(String clave) {
    fallos.remove(clave);
  }
}
