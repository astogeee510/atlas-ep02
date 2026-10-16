package cl.duoc.atlas.domain.exception;

/** Se lanza cuando se intenta registrar un RUT que ya existe. HTTP 409. */
public class RecursoDuplicadoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RecursoDuplicadoException(String mensaje) {
    super(mensaje);
  }
}
