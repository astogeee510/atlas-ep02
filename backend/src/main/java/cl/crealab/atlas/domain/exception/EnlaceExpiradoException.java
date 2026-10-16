package cl.crealab.atlas.domain.exception;

/** El enlace compartido expiro o fue revocado (HTTP 410, RF-5.5). */
public class EnlaceExpiradoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EnlaceExpiradoException(String mensaje) {
    super(mensaje);
  }
}
