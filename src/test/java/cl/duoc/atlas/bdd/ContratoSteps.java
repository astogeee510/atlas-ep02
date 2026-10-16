package cl.duoc.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.atlas.application.usecase.ContratoUseCase;
import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/** Step definitions de estado_contrato.feature. */
public class ContratoSteps {

  private static final ZoneId ZONA = ZoneId.of("America/Santiago");

  private final MemoriaGateways.Clientes clientes = new MemoriaGateways.Clientes();
  private final MemoriaGateways.Contratos contratos = new MemoriaGateways.Contratos();

  private ContratoUseCase useCase;
  private LocalDate hoy;
  private Cliente cliente;
  private Contrato contrato;

  private RuntimeException error;

  private void ejecutar(Runnable accion) {
    error = null;
    try {
      accion.run();
    } catch (RuntimeException e) {
      error = e;
    }
  }

  @Dado("que hoy es {string}")
  public void hoyEs(String fecha) {
    hoy = LocalDate.parse(fecha);
    useCase =
        new ContratoUseCase(
            contratos, clientes, Clock.fixed(hoy.atStartOfDay(ZONA).toInstant(), ZONA));
  }

  @Dado("un cliente activo")
  public void unClienteActivo() {
    cliente =
        clientes.save(
            new Cliente(null, "12345678-5", "Cliente", null, "Dir", "c@a.cl", "12345678", true));
  }

  @Cuando("registro un contrato que termina el {string}")
  public void registroContrato(String termino) {
    contrato =
        useCase.crear(
            cliente.getId(),
            new Contrato(null, null, "Contrato", null, hoy.minusYears(1), LocalDate.parse(termino)),
            "admin");
  }

  @Cuando("intento registrar un contrato del {string} al {string}")
  public void intentoRegistrar(String inicio, String termino) {
    ejecutar(
        () ->
            useCase.crear(
                cliente.getId(),
                new Contrato(
                    null, null, "Mal", null, LocalDate.parse(inicio), LocalDate.parse(termino)),
                "admin"));
  }

  @Entonces("el estado del contrato es {string}")
  public void estadoEs(String estado) {
    assertThat(useCase.estado(contrato).name()).isEqualTo(estado);
  }

  @Entonces("el contrato es rechazado por fechas inválidas")
  public void rechazado() {
    assertThat(error).isInstanceOf(DatosInvalidosException.class);
    assertThat(contratos.findByClienteId(cliente.getId())).isEmpty();
  }
}
