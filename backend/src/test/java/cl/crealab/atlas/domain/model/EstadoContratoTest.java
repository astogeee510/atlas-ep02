package cl.crealab.atlas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** CP-FUN-05: estado del contrato en valores limite (RF-5.4). */
class EstadoContratoTest {

  private static final LocalDate HOY = LocalDate.of(2026, 10, 16);

  @Test
  void activoCuandoFaltanMasDeTreintaDias() {
    assertThat(EstadoContrato.calcular(HOY.plusDays(31), HOY, 30)).isEqualTo(EstadoContrato.ACTIVO);
  }

  @Test
  void porVencerJustoEnElLimiteDeTreintaDias() {
    assertThat(EstadoContrato.calcular(HOY.plusDays(30), HOY, 30))
        .isEqualTo(EstadoContrato.POR_VENCER);
  }

  @Test
  void porVencerElMismoDiaDeTermino() {
    assertThat(EstadoContrato.calcular(HOY, HOY, 30)).isEqualTo(EstadoContrato.POR_VENCER);
  }

  @Test
  void expiradoAlDiaSiguienteDelTermino() {
    assertThat(EstadoContrato.calcular(HOY.minusDays(1), HOY, 30))
        .isEqualTo(EstadoContrato.EXPIRADO);
  }

  @Test
  void respetaDiasDeAvisoConfigurables() {
    assertThat(EstadoContrato.calcular(HOY.plusDays(10), HOY, 5)).isEqualTo(EstadoContrato.ACTIVO);
    assertThat(EstadoContrato.calcular(HOY.plusDays(5), HOY, 5))
        .isEqualTo(EstadoContrato.POR_VENCER);
  }
}
