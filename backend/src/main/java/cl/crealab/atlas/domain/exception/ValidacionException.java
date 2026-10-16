package cl.crealab.atlas.domain.exception;

import java.util.ArrayList;
import java.util.List;

/** Los datos no cumplen las reglas de validacion del negocio (HTTP 400). */
public class ValidacionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final ArrayList<String> errores;

  public ValidacionException(String mensaje, List<String> errores) {
    super(mensaje);
    this.errores = new ArrayList<>(errores);
  }

  public ValidacionException(String mensaje) {
    this(mensaje, List.of(mensaje));
  }

  public List<String> getErrores() {
    return List.copyOf(errores);
  }
}
