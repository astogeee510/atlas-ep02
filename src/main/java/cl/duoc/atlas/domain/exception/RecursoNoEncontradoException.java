package cl.duoc.atlas.domain.exception;

/** Se lanza cuando un cliente o contrato no existe (o fue eliminado logicamente). HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RecursoNoEncontradoException(String recurso, Long id) {
    super("No existe un " + recurso + " con id " + id);
  }
}
