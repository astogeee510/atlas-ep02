package cl.crealab.atlas.domain.exception;

/** Credenciales o token invalidos (HTTP 401, RF-1). */
public class CredencialesInvalidasException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CredencialesInvalidasException(String mensaje) {
    super(mensaje);
  }
}
