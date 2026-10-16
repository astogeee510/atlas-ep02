package cl.crealab.atlas.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** RF-4.3: validacion de formato del RUT. */
class ValidadorRutTest {

  @ParameterizedTest
  @ValueSource(strings = {"12.345.678-5", "12345678-5", "123456785", "76123456-0", "10000013-k"})
  void aceptaRutValidos(String rut) {
    assertThat(ValidadorRut.esValido(rut)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12.345.678-4", "1-9", "abc", "12345678-X", "123456789012-3", " "})
  void rechazaRutInvalidos(String rut) {
    assertThat(ValidadorRut.esValido(rut)).isFalse();
  }

  @Test
  void rechazaNulo() {
    assertThat(ValidadorRut.esValido(null)).isFalse();
  }

  @Test
  void normalizaFormato() {
    assertThat(ValidadorRut.normalizar("10.000.013-k")).isEqualTo("10000013-K");
    assertThat(ValidadorRut.normalizar("12345678 5")).isEqualTo("12345678-5");
    assertThat(ValidadorRut.normalizar(null)).isNull();
    assertThat(ValidadorRut.normalizar("")).isNull();
    assertThat(ValidadorRut.normalizar("5")).isEqualTo("5");
  }

  @Test
  void calculaDigitoVerificador() {
    assertThat(ValidadorRut.calcularDigito("12345678")).isEqualTo('5');
    assertThat(ValidadorRut.calcularDigito("10000013")).isEqualTo('K');
    assertThat(ValidadorRut.calcularDigito("76123456")).isEqualTo('0');
    assertThat(ValidadorRut.calcularDigito("78111222")).isEqualTo('4');
  }
}
