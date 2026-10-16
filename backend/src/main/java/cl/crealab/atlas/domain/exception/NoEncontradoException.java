package cl.crealab.atlas.domain.exception;

/** El recurso solicitado no existe o no es visible para el usuario (HTTP 404). */
public class NoEncontradoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoEncontradoException(String mensaje) {
    super(mensaje);
  }
}
