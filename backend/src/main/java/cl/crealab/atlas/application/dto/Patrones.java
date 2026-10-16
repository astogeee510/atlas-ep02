package cl.crealab.atlas.application.dto;

/** Expresiones regulares de validacion de entrada (RF-4.3, NFR-SEG-5). */
public final class Patrones {

  /** Letras (con tildes), numeros y puntuacion basica; impide etiquetas HTML. */
  public static final String NOMBRE = "^[\\p{L}\\p{N} .,&'()-]{2,120}$";

  /** Telefono chileno o internacional: digitos, espacios y + inicial. */
  public static final String TELEFONO = "^\\+?[0-9 ]{8,20}$";

  /** Nombre de usuario alfanumerico. */
  public static final String USUARIO = "^[a-zA-Z0-9._-]{4,50}$";

  /** Numero de cuenta bancaria: digitos y guiones. */
  public static final String CUENTA = "^[0-9-]{4,20}$";

  /** Nombre de banco. */
  public static final String BANCO = "^[\\p{L}\\p{N} .&-]{2,60}$";

  private Patrones() {}
}
