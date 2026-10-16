package cl.duoc.atlas.domain.exception;

/** Se lanza tras demasiados intentos de login fallidos (proteccion fuerza bruta). HTTP 429. */
public class CuentaBloqueadaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CuentaBloqueadaException(long minutos) {
    super("Demasiados intentos fallidos. Intente nuevamente en " + minutos + " minutos");
  }
}
