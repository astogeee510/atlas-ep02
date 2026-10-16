package cl.crealab.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.domain.model.EstadoContrato;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.time.LocalDate;

/** Pasos del estado de contrato. */
public class EstadoContratoSteps {

  private LocalDate hoy;
  private EstadoContrato estado;

  @Dado("que hoy es {string}")
  public void queHoyEs(String fecha) {
    hoy = LocalDate.parse(fecha);
  }

  @Cuando("un contrato termina el {string}")
  public void unContratoTermina(String fecha) {
    estado = EstadoContrato.calcular(LocalDate.parse(fecha), hoy, 30);
  }

  @Entonces("su estado es {string}")
  public void suEstadoEs(String esperado) {
    assertThat(estado).isEqualTo(EstadoContrato.valueOf(esperado));
  }
}
