package cl.crealab.atlas.domain.exception;

/** La operacion viola una regla de unicidad o de negocio (HTTP 409). */
public class ConflictoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ConflictoException(String mensaje) {
    super(mensaje);
  }
}
