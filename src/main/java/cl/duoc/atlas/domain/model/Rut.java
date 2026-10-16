package cl.duoc.atlas.domain.model;

import java.util.Locale;

/** Reglas del RUT chileno: normalizacion y digito verificador modulo 11 (RF-4.3). */
public final class Rut {

  private static final int MODULO = 11;
  private static final int MULTIPLICADOR_MAXIMO = 7;

  private Rut() {}

  /**
   * Normaliza un RUT quitando puntos y espacios y dejando el guion antes del digito verificador.
   *
   * @param rut RUT en cualquier formato (12.345.678-5, 12345678-5 o 123456785)
   * @return RUT normalizado (12345678-5) o null si viene vacio
   */
  public static String normalizar(String rut) {
    if (rut == null || rut.isBlank()) {
      return null;
    }
    String limpio = rut.replace(".", "").replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    if (limpio.length() < 2) {
      return limpio;
    }
    return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
  }

  /**
   * Indica si el RUT es valido.
   *
   * @param rut RUT en cualquier formato
   * @return true si el cuerpo es numerico y el digito verificador es correcto
   */
  public static boolean esValido(String rut) {
    String normalizado = normalizar(rut);
    if (normalizado == null || !normalizado.matches("\\d{7,8}-[\\dK]")) {
      return false;
    }
    String cuerpo = normalizado.substring(0, normalizado.indexOf('-'));
    char dv = normalizado.charAt(normalizado.length() - 1);
    return calcularDigito(cuerpo) == dv;
  }

  /**
   * Calcula el digito verificador de un cuerpo de RUT.
   *
   * @param cuerpo digitos del RUT sin verificador
   * @return digito verificador ('0'-'9' o 'K')
   */
  public static char calcularDigito(String cuerpo) {
    int suma = 0;
    int multiplicador = 2;
    for (int i = cuerpo.length() - 1; i >= 0; i--) {
      suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
      multiplicador = multiplicador == MULTIPLICADOR_MAXIMO ? 2 : multiplicador + 1;
    }
    int resto = MODULO - (suma % MODULO);
    if (resto == MODULO) {
      return '0';
    }
    if (resto == MODULO - 1) {
      return 'K';
    }
    return Character.forDigit(resto, 10);
  }
}
