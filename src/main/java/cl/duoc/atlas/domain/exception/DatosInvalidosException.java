package cl.duoc.atlas.domain.exception;

/**
 * Se lanza cuando los datos no cumplen una regla de negocio (RUT invalido, fechas incoherentes,
 * eliminacion sin confirmar, paginacion fuera de rango). HTTP 400.
 */
public class DatosInvalidosException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DatosInvalidosException(String mensaje) {
    super(mensaje);
  }
}
