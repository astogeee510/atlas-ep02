package cl.duoc.atlas.application.usecase;

import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.EstadoContrato;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de contratos (RF-5.1, RF-5.4). Un contrato pertenece a un unico cliente activo
 * (RB-3) y su estado se calcula con la fecha actual del reloj inyectado.
 */
@Service
public class ContratoUseCase {

  private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

  private final ContratoGateway contratoGateway;
  private final ClienteGateway clienteGateway;
  private final Clock clock;

  public ContratoUseCase(
      ContratoGateway contratoGateway, ClienteGateway clienteGateway, Clock clock) {
    this.contratoGateway = contratoGateway;
    this.clienteGateway = clienteGateway;
    this.clock = clock;
  }

  /**
   * Lista los contratos de un cliente activo (NFR-USAB-4: primero cliente, luego contratos).
   *
   * @param clienteId id del cliente
   * @return contratos del cliente
   */
  public List<Contrato> listarPorCliente(Long clienteId) {
    clienteActivo(clienteId);
    return contratoGateway.findByClienteId(clienteId);
  }

  /**
   * Obtiene un contrato.
   *
   * @param id id del contrato
   * @return el contrato
   */
  public Contrato obtener(Long id) {
    return contratoGateway
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("contrato", id));
  }

  /**
   * Crea un contrato asociado a un cliente activo.
   *
   * @param clienteId id del cliente
   * @param contrato datos del contrato
   * @param usuario usuario que ejecuta la accion (auditoria)
   * @return contrato creado
   */
  public Contrato crear(Long clienteId, Contrato contrato, String usuario) {
    clienteActivo(clienteId);
    validarFechas(contrato);
    contrato.setId(null);
    contrato.setClienteId(clienteId);
    Contrato creado = contratoGateway.save(contrato);
    AUDITORIA.info(
        "accion=CREAR_CONTRATO usuario={} clienteId={} contratoId={}",
        usuario,
        clienteId,
        creado.getId());
    return creado;
  }

  /**
   * Actualiza titulo, descripcion y fechas de un contrato. El cliente asociado no cambia (RB-3).
   *
   * @param id id del contrato
   * @param cambios nuevos datos
   * @param usuario usuario que ejecuta la accion (auditoria)
   * @return contrato actualizado
   */
  public Contrato actualizar(Long id, Contrato cambios, String usuario) {
    Contrato actual = obtener(id);
    validarFechas(cambios);
    actual.setTitulo(cambios.getTitulo());
    actual.setDescripcion(cambios.getDescripcion());
    actual.setFechaInicio(cambios.getFechaInicio());
    actual.setFechaTermino(cambios.getFechaTermino());
    Contrato guardado = contratoGateway.save(actual);
    AUDITORIA.info("accion=EDITAR_CONTRATO usuario={} contratoId={}", usuario, id);
    return guardado;
  }

  /**
   * Elimina un contrato.
   *
   * @param id id del contrato
   * @param usuario usuario que ejecuta la accion (auditoria)
   */
  public void eliminar(Long id, String usuario) {
    obtener(id);
    contratoGateway.deleteById(id);
    AUDITORIA.info("accion=ELIMINAR_CONTRATO usuario={} contratoId={}", usuario, id);
  }

  /**
   * Calcula el estado actual del contrato (RF-5.4).
   *
   * @param contrato contrato
   * @return estado a la fecha de hoy
   */
  public EstadoContrato estado(Contrato contrato) {
    return contrato.estado(LocalDate.now(clock));
  }

  private Cliente clienteActivo(Long clienteId) {
    return clienteGateway
        .findById(clienteId)
        .filter(Cliente::isActivo)
        .orElseThrow(() -> new RecursoNoEncontradoException("cliente", clienteId));
  }

  private void validarFechas(Contrato contrato) {
    if (!contrato.tieneFechasValidas()) {
      throw new DatosInvalidosException(
          "La fecha de término no puede ser anterior a la fecha de inicio");
    }
  }
}
