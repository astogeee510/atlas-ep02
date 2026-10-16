package cl.crealab.atlas.domain.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Politica de contrasenas del ERS (2.4): largo minimo configurable (por defecto 12) e incluir
 * mayusculas, minusculas, numeros y simbolos.
 */
public class PoliticaContrasena {

  private final int largoMinimo;

  public PoliticaContrasena(int largoMinimo) {
    this.largoMinimo = largoMinimo;
  }

  /**
   * Valida una contrasena.
   *
   * @param contrasena contrasena en texto plano (nunca se registra en logs)
   * @return lista de reglas incumplidas; vacia si la contrasena es valida
   */
  public List<String> validar(String contrasena) {
    List<String> errores = new ArrayList<>();
    if (contrasena == null || contrasena.length() < largoMinimo) {
      errores.add("Debe tener al menos " + largoMinimo + " caracteres");
    }
    String valor = contrasena == null ? "" : contrasena;
    if (valor.chars().noneMatch(Character::isUpperCase)) {
      errores.add("Debe incluir al menos una letra mayuscula");
    }
    if (valor.chars().noneMatch(Character::isLowerCase)) {
      errores.add("Debe incluir al menos una letra minuscula");
    }
    if (valor.chars().noneMatch(Character::isDigit)) {
      errores.add("Debe incluir al menos un numero");
    }
    if (valor.chars().allMatch(Character::isLetterOrDigit)) {
      errores.add("Debe incluir al menos un simbolo");
    }
    return errores;
  }

  public int getLargoMinimo() {
    return largoMinimo;
  }
}
