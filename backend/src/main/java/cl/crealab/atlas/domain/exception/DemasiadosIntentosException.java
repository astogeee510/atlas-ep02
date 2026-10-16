package cl.crealab.atlas.domain.exception;

/** Se supero el limite de intentos de login fallidos (HTTP 429). */
public class DemasiadosIntentosException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DemasiadosIntentosException(String mensaje) {
    super(mensaje);
  }
}
