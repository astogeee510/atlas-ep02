package cl.crealab.atlas.domain.exception;

/** El usuario no tiene permisos para la operacion (HTTP 403, NFR-SEG-4). */
public class AccesoDenegadoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AccesoDenegadoException(String mensaje) {
    super(mensaje);
  }
}
