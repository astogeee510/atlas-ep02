package cl.duoc.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.atlas.application.usecase.ClienteUseCase;
import cl.duoc.atlas.domain.model.Cliente;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

/** Step definitions de gestion_clientes.feature. Cucumber crea una instancia por escenario. */
public class ClienteSteps {

  private final MemoriaGateways.Clientes gateway = new MemoriaGateways.Clientes();
  private final ClienteUseCase useCase = new ClienteUseCase(gateway);

  private Cliente registrado;

  private RuntimeException error;

  private void ejecutar(Runnable accion) {
    error = null;
    try {
      accion.run();
    } catch (RuntimeException e) {
      error = e;
    }
  }

  @Dado("que no existe un cliente con RUT {string}")
  public void queNoExiste(String rut) {
    assertThat(gateway.findByRut(rut)).isEmpty();
  }

  @Dado("que ya existe un cliente con RUT {string}")
  public void queYaExiste(String rut) {
    useCase.crear(cliente(rut, "Existente"), "admin");
  }

  @Cuando("registro un cliente con RUT {string} y nombre {string}")
  public void registro(String rut, String nombre) {
    registrado = useCase.crear(cliente(rut, nombre), "admin");
  }

  @Cuando("intento registrar otro cliente con RUT {string}")
  public void intentoRegistrar(String rut) {
    ejecutar(() -> useCase.crear(cliente(rut, "Otro"), "admin"));
  }

  @Cuando("intento eliminar el cliente {string} sin confirmar")
  public void eliminarSinConfirmar(String rut) {
    Long id = gateway.findByRut(rut).orElseThrow().getId();
    ejecutar(() -> useCase.eliminar(id, false, "admin"));
  }

  @Cuando("elimino el cliente {string} confirmando")
  public void eliminarConfirmando(String rut) {
    useCase.eliminar(gateway.findByRut(rut).orElseThrow().getId(), true, "admin");
  }

  @Entonces("el cliente {string} queda registrado")
  public void quedaRegistrado(String rut) {
    assertThat(registrado.getId()).isNotNull();
    assertThat(registrado.getRut()).isEqualTo(rut);
  }

  @Entonces("la lista de clientes tiene {int} elemento(s)")
  public void listaTiene(int cantidad) {
    assertThat(useCase.listar("", 0, 10).contenido()).hasSize(cantidad);
  }

  @Entonces("la operación falla con el mensaje {string}")
  public void fallaConMensaje(String mensaje) {
    assertThat(error).isNotNull().hasMessage(mensaje);
  }

  @Entonces("el cliente {string} sigue guardado como inactivo")
  public void sigueGuardado(String rut) {
    assertThat(gateway.findByRut(rut)).get().extracting(Cliente::isActivo).isEqualTo(false);
  }

  private Cliente cliente(String rut, String nombre) {
    return new Cliente(null, rut, nombre, null, "Av. Matta 500", "c@atlas.cl", "56912345678", true);
  }
}
