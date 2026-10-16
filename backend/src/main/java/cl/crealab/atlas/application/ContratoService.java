package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.ContratoRequest;
import cl.crealab.atlas.application.dto.ContratoResponse;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Cliente;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.EstadoContrato;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.DocumentoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion de contratos de un cliente (RF-5.1, RF-5.4, RB-3). */
@Service
public class ContratoService {

  private final ContratoRepository contratos;
  private final ClienteRepository clientes;
  private final DocumentoRepository documentos;
  private final ControlAcceso acceso;
  private final AuditoriaService auditoria;
  private final Clock clock;
  private final int diasAviso;

  public ContratoService(
      ContratoRepository contratos,
      ClienteRepository clientes,
      DocumentoRepository documentos,
      ControlAcceso acceso,
      AuditoriaService auditoria,
      Clock clock,
      @Value("${atlas.contratos.dias-aviso-vencimiento}") int diasAviso) {
    this.contratos = contratos;
    this.clientes = clientes;
    this.documentos = documentos;
    this.acceso = acceso;
    this.auditoria = auditoria;
    this.clock = clock;
    this.diasAviso = diasAviso;
  }

  /**
   * Lista los contratos de un cliente (NFR-USAB-4: primero clientes, luego sus contratos).
   *
   * @param actorId usuario
   * @param clienteId cliente
   * @return contratos ordenados por fecha de termino
   */
  @Transactional(readOnly = true)
  public List<ContratoResponse> listarPorCliente(Long actorId, Long clienteId) {
    Cliente cliente = buscarCliente(clienteId);
    acceso.exigirMiembro(actorId, cliente.getEmpresaId());
    return contratos.findByClienteIdAndEliminadoFalseOrderByFechaTerminoAsc(clienteId).stream()
        .map(this::respuesta)
        .toList();
  }

  /**
   * Obtiene un contrato.
   *
   * @param actorId usuario
   * @param contratoId contrato
   * @return contrato con estado calculado
   */
  @Transactional(readOnly = true)
  public ContratoResponse obtener(Long actorId, Long contratoId) {
    Contrato contrato = buscar(contratoId);
    acceso.exigirMiembro(actorId, contrato.getEmpresaId());
    return respuesta(contrato);
  }

  /**
   * Crea un contrato para un cliente (RF-5.1).
   *
   * @param actorId administrador
   * @param solicitud datos
   * @return contrato creado
   */
  @Transactional
  public ContratoResponse crear(Long actorId, ContratoRequest solicitud) {
    Cliente cliente = buscarCliente(solicitud.clienteId());
    acceso.exigirAdmin(actorId, cliente.getEmpresaId());
    validarFechas(solicitud);
    Contrato contrato =
        new Contrato(cliente.getEmpresaId(), cliente.getId(), solicitud.titulo().trim());
    aplicar(contrato, solicitud);
    contrato = contratos.save(contrato);
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.CREAR_CONTRATO,
        "contrato",
        contrato.getId(),
        "Contrato creado");
    return respuesta(contrato);
  }

  /**
   * Actualiza un contrato. No puede cambiar de cliente (RB-3).
   *
   * @param actorId administrador
   * @param contratoId contrato
   * @param solicitud datos
   * @return contrato actualizado
   */
  @Transactional
  public ContratoResponse actualizar(Long actorId, Long contratoId, ContratoRequest solicitud) {
    Contrato contrato = buscar(contratoId);
    acceso.exigirAdmin(actorId, contrato.getEmpresaId());
    if (!contrato.getClienteId().equals(solicitud.clienteId())) {
      throw new ValidacionException("Un contrato no puede cambiar de cliente (RB-3)");
    }
    validarFechas(solicitud);
    contrato.setTitulo(solicitud.titulo().trim());
    aplicar(contrato, solicitud);
    contrato = contratos.save(contrato);
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.EDITAR_CONTRATO,
        "contrato",
        contrato.getId(),
        "Contrato editado");
    return respuesta(contrato);
  }

  /**
   * Elimina logicamente un contrato.
   *
   * @param actorId administrador
   * @param contratoId contrato
   */
  @Transactional
  public void eliminar(Long actorId, Long contratoId) {
    Contrato contrato = buscar(contratoId);
    acceso.exigirAdmin(actorId, contrato.getEmpresaId());
    contrato.setEliminado(true);
    contratos.save(contrato);
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.ELIMINAR_CONTRATO,
        "contrato",
        contratoId,
        "Eliminacion logica");
  }

  private ContratoResponse respuesta(Contrato contrato) {
    EstadoContrato estado =
        EstadoContrato.calcular(contrato.getFechaTermino(), LocalDate.now(clock), diasAviso);
    return new ContratoResponse(
        contrato.getId(),
        contrato.getClienteId(),
        contrato.getTitulo(),
        contrato.getDescripcion(),
        contrato.getFechaInicio(),
        contrato.getFechaTermino(),
        estado,
        documentos.findByContratoId(contrato.getId()).isPresent());
  }

  private Contrato buscar(Long contratoId) {
    return contratos
        .findByIdAndEliminadoFalse(contratoId)
        .orElseThrow(() -> new NoEncontradoException("Contrato no encontrado"));
  }

  private Cliente buscarCliente(Long clienteId) {
    return clientes
        .findByIdAndEliminadoFalse(clienteId)
        .orElseThrow(() -> new NoEncontradoException("Cliente no encontrado"));
  }

  private static void validarFechas(ContratoRequest solicitud) {
    if (solicitud.fechaTermino().isBefore(solicitud.fechaInicio())) {
      throw new ValidacionException("La fecha de termino no puede ser anterior a la de inicio");
    }
  }

  private static void aplicar(Contrato contrato, ContratoRequest solicitud) {
    contrato.setDescripcion(solicitud.descripcion());
    contrato.setFechaInicio(solicitud.fechaInicio());
    contrato.setFechaTermino(solicitud.fechaTermino());
  }
}
