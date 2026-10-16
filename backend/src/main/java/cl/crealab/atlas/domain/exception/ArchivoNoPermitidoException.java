package cl.crealab.atlas.domain.exception;

/** El archivo no corresponde a un tipo permitido (HTTP 415, RF-5.2). */
public class ArchivoNoPermitidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ArchivoNoPermitidoException(String mensaje) {
    super(mensaje);
  }
}
