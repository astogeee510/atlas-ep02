package cl.crealab.atlas.domain.service;

import java.util.Locale;

/** Valida el RUT chileno con su digito verificador (modulo 11). */
public final class ValidadorRut {

  private ValidadorRut() {}

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
    String limpio = rut.replace(".", "").replace("-", "").replace(" ", "");
    limpio = limpio.toUpperCase(Locale.ROOT);
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
      multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
    }
    int resto = 11 - (suma % 11);
    if (resto == 11) {
      return '0';
    }
    if (resto == 10) {
      return 'K';
    }
    return Character.forDigit(resto, 10);
  }
}
