package cl.duoc.atlas.domain.exception;

/**
 * Login fallido. El mensaje es el mismo si el usuario no existe o si la contrasena es incorrecta,
 * para no revelar que usuarios existen. HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CredencialesInvalidasException() {
    super("Usuario o contraseña incorrectos");
  }
}
