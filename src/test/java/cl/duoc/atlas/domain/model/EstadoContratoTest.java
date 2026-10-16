package cl.duoc.atlas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** CP-CON-02: estado del contrato calculado segun la fecha de termino (RF-5.4). */
class EstadoContratoTest {

  private static final LocalDate HOY = LocalDate.of(2026, 10, 16);

  @ParameterizedTest(name = "termino {0} -> {1}")
  @CsvSource({
    "2026-10-15, EXPIRADO",
    "2026-10-16, POR_VENCER",
    "2026-11-15, POR_VENCER",
    "2026-11-16, ACTIVO",
    "2027-10-16, ACTIVO"
  })
  void calculaElEstadoEnLosLimites(LocalDate termino, EstadoContrato esperado) {
    assertThat(EstadoContrato.calcular(termino, HOY)).isEqualTo(esperado);
  }

  @Test
  void elAvisoEsDeTreintaDias() {
    assertThat(EstadoContrato.DIAS_AVISO).isEqualTo(30);
    assertThat(EstadoContrato.calcular(HOY.plusDays(30), HOY)).isEqualTo(EstadoContrato.POR_VENCER);
    assertThat(EstadoContrato.calcular(HOY.plusDays(31), HOY)).isEqualTo(EstadoContrato.ACTIVO);
  }
}
