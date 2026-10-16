package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.ClienteRequest;
import cl.crealab.atlas.application.dto.ClienteResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Cliente;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.domain.service.ValidadorRut;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion de clientes (RF-4): listado paginado con filtro, CRUD y eliminacion logica. */
@Service
public class ClienteService {

  private final ClienteRepository clientes;
  private final ContratoRepository contratos;
  private final ControlAcceso acceso;
  private final AuditoriaService auditoria;
  private final Clock clock;

  public ClienteService(
      ClienteRepository clientes,
      ContratoRepository contratos,
      ControlAcceso acceso,
      AuditoriaService auditoria,
      Clock clock) {
    this.clientes = clientes;
    this.contratos = contratos;
    this.acceso = acceso;
    this.auditoria = auditoria;
    this.clock = clock;
  }

  /**
   * Lista los clientes de la empresa con paginacion y filtro por nombre (RF-4.2).
   *
   * @param actorId usuario
   * @param empresaId empresa
   * @param nombre texto a buscar en el nombre (opcional)
   * @param pagina pagina desde 0
   * @param tamano tamano de pagina (maximo 50)
   * @return pagina de clientes
   */
  @Transactional(readOnly = true)
  public PaginaResponse<ClienteResponse> listar(
      Long actorId, Long empresaId, String nombre, int pagina, int tamano) {
    Rol rol = acceso.exigirMiembro(actorId, empresaId);
    String filtro = nombre == null ? "" : nombre.trim();
    PageRequest pageRequest =
        PageRequest.of(
            PaginaResponse.paginaSegura(pagina),
            PaginaResponse.tamanoSeguro(tamano),
            Sort.by("nombre"));
    return PaginaResponse.de(
        clientes.findByEmpresaIdAndEliminadoFalseAndNombreContainingIgnoreCase(
            empresaId, filtro, pageRequest),
        c -> ClienteResponse.desde(c, rol));
  }

  /**
   * Obtiene un cliente.
   *
   * @param actorId usuario
   * @param clienteId cliente
   * @return cliente
   */
  @Transactional(readOnly = true)
  public ClienteResponse obtener(Long actorId, Long clienteId) {
    Cliente cliente = buscar(clienteId);
    Rol rol = acceso.exigirMiembro(actorId, cliente.getEmpresaId());
    return ClienteResponse.desde(cliente, rol);
  }

  /**
   * Crea un cliente (RF-4.1). Solo administradores (RB-4, RB-5).
   *
   * @param actorId administrador
   * @param empresaId empresa
   * @param solicitud datos
   * @return cliente creado
   */
  @Transactional
  public ClienteResponse crear(Long actorId, Long empresaId, ClienteRequest solicitud) {
    acceso.exigirAdmin(actorId, empresaId);
    validarRut(solicitud.rut());
    Cliente cliente = new Cliente(empresaId, solicitud.nombre().trim());
    aplicar(cliente, solicitud);
    cliente = clientes.save(cliente);
    auditoria.registrar(
        empresaId,
        actorId,
        AccionAuditoria.CREAR_CLIENTE,
        "cliente",
        cliente.getId(),
        "Cliente creado");
    return ClienteResponse.desde(cliente, Rol.ADMIN);
  }

  /**
   * Actualiza un cliente (RF-4.2).
   *
   * @param actorId administrador
   * @param clienteId cliente
   * @param solicitud datos
   * @return cliente actualizado
   */
  @Transactional
  public ClienteResponse actualizar(Long actorId, Long clienteId, ClienteRequest solicitud) {
    Cliente cliente = buscar(clienteId);
    acceso.exigirAdmin(actorId, cliente.getEmpresaId());
    validarRut(solicitud.rut());
    cliente.setNombre(solicitud.nombre().trim());
    aplicar(cliente, solicitud);
    cliente.setActualizadoEn(clock.instant());
    cliente = clientes.save(cliente);
    auditoria.registrar(
        cliente.getEmpresaId(),
        actorId,
        AccionAuditoria.EDITAR_CLIENTE,
        "cliente",
        cliente.getId(),
        "Cliente editado");
    return ClienteResponse.desde(cliente, Rol.ADMIN);
  }

  /**
   * Elimina logicamente un cliente y sus contratos (criterio de aceptacion RF-4).
   *
   * @param actorId administrador
   * @param clienteId cliente
   */
  @Transactional
  public void eliminar(Long actorId, Long clienteId) {
    Cliente cliente = buscar(clienteId);
    acceso.exigirAdmin(actorId, cliente.getEmpresaId());
    Instant ahora = clock.instant();
    cliente.eliminarLogicamente(ahora);
    clientes.save(cliente);
    List<Contrato> asociados =
        contratos.findByClienteIdAndEliminadoFalseOrderByFechaTerminoAsc(clienteId);
    asociados.forEach(c -> c.setEliminado(true));
    contratos.saveAll(asociados);
    auditoria.registrar(
        cliente.getEmpresaId(),
        actorId,
        AccionAuditoria.ELIMINAR_CLIENTE,
        "cliente",
        clienteId,
        "Eliminacion logica (" + asociados.size() + " contratos)");
  }

  private Cliente buscar(Long clienteId) {
    return clientes
        .findByIdAndEliminadoFalse(clienteId)
        .orElseThrow(() -> new NoEncontradoException("Cliente no encontrado"));
  }

  private static void validarRut(String rut) {
    if (rut != null && !rut.isBlank() && !ValidadorRut.esValido(rut)) {
      throw new ValidacionException("El RUT no es valido");
    }
  }

  private static void aplicar(Cliente cliente, ClienteRequest solicitud) {
    cliente.setRut(ValidadorRut.normalizar(solicitud.rut()));
    cliente.setDescripcion(solicitud.descripcion());
    cliente.setDireccion(solicitud.direccion());
    cliente.setCorreoContacto(solicitud.correoContacto());
    cliente.setTelefonoContacto(solicitud.telefonoContacto());
    cliente.setBanco(solicitud.banco());
    cliente.setNumeroCuenta(solicitud.numeroCuenta());
  }
}
