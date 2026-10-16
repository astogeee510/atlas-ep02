package cl.duoc.atlas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** CP-CLI-03: validacion del RUT con digito verificador (RF-4.3). */
class RutTest {

  @ParameterizedTest
  @ValueSource(strings = {"12.345.678-5", "12345678-5", "123456785", "76123456-0", "9.876.543-3"})
  void aceptaRutValidosEnCualquierFormato(String rut) {
    assertThat(Rut.esValido(rut)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12.345.678-9", "1-9", "abc", "12345678-X", "123456789012-3", "5"})
  void rechazaRutInvalidos(String rut) {
    assertThat(Rut.esValido(rut)).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void rutVacioNoEsValidoYNoSeNormaliza(String rut) {
    assertThat(Rut.esValido(rut)).isFalse();
    assertThat(Rut.normalizar(rut)).isNull();
  }

  @Test
  void normalizaQuitandoPuntosYDejandoGuion() {
    assertThat(Rut.normalizar("12.345.678-5")).isEqualTo("12345678-5");
    assertThat(Rut.normalizar("12 345 678 5")).isEqualTo("12345678-5");
    assertThat(Rut.normalizar("20.202.020-k")).isEqualTo("20202020-K");
    assertThat(Rut.normalizar("5")).isEqualTo("5");
  }

  @Test
  void calculaDigitosEspeciales() {
    assertThat(Rut.calcularDigito("12345678")).isEqualTo('5');
    assertThat(Rut.calcularDigito("76123456")).isEqualTo('0');
    assertThat(Rut.calcularDigito("11111111")).isEqualTo('1');
    assertThat(Rut.calcularDigito("20202020")).isEqualTo('8');
    assertThat(Rut.calcularDigito("10000013")).isEqualTo('K');
    assertThat(Rut.esValido("10.000.013-K")).isTrue();
    assertThat(Rut.esValido("15.000.000-9")).isTrue();
  }
}
