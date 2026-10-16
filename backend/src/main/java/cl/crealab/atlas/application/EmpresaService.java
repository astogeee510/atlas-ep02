package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.DashboardResponse;
import cl.crealab.atlas.application.dto.EmpresaRequest;
import cl.crealab.atlas.application.dto.EmpresaResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.Empresa;
import cl.crealab.atlas.domain.model.EstadoContrato;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.EmpresaRepository;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creacion de empresas y dashboard (RF-2.2, RF-2.3). */
@Service
public class EmpresaService {

  private final EmpresaRepository empresas;
  private final MembresiaRepository membresias;
  private final ClienteRepository clientes;
  private final ContratoRepository contratos;
  private final ControlAcceso acceso;
  private final AuditoriaService auditoria;
  private final Clock clock;
  private final int diasAviso;

  public EmpresaService(
      EmpresaRepository empresas,
      MembresiaRepository membresias,
      ClienteRepository clientes,
      ContratoRepository contratos,
      ControlAcceso acceso,
      AuditoriaService auditoria,
      Clock clock,
      @Value("${atlas.contratos.dias-aviso-vencimiento}") int diasAviso) {
    this.empresas = empresas;
    this.membresias = membresias;
    this.clientes = clientes;
    this.contratos = contratos;
    this.acceso = acceso;
    this.auditoria = auditoria;
    this.clock = clock;
    this.diasAviso = diasAviso;
  }

  /**
   * Crea una empresa; quien la crea queda como administrador (RF-2.3).
   *
   * @param actorId usuario que crea la empresa
   * @param solicitud datos de la empresa
   * @return empresa creada
   */
  @Transactional
  public EmpresaResponse crear(Long actorId, EmpresaRequest solicitud) {
    String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
    if (empresas.existsByCorreoIgnoreCase(correo)) {
      throw new ConflictoException("Ya existe una empresa con ese correo principal (RB-1)");
    }
    Empresa empresa =
        empresas.save(
            new Empresa(
                solicitud.nombre().trim(), solicitud.direccion(), solicitud.telefono(), correo));
    membresias.save(new Membresia(empresa.getId(), actorId, Rol.ADMIN));
    auditoria.registrar(
        empresa.getId(),
        actorId,
        AccionAuditoria.CREAR_EMPRESA,
        "empresa",
        empresa.getId(),
        "Empresa creada");
    return EmpresaResponse.desde(empresa);
  }

  /**
   * Obtiene una empresa visible para el usuario.
   *
   * @param actorId usuario
   * @param empresaId empresa
   * @return datos de la empresa
   */
  @Transactional(readOnly = true)
  public EmpresaResponse obtener(Long actorId, Long empresaId) {
    acceso.exigirMiembro(actorId, empresaId);
    return empresas
        .findById(empresaId)
        .map(EmpresaResponse::desde)
        .orElseThrow(() -> new NoEncontradoException("Empresa no encontrada"));
  }

  /**
   * Indicadores del dashboard de la empresa.
   *
   * @param actorId usuario
   * @param empresaId empresa
   * @return totales de clientes y contratos por estado
   */
  @Transactional(readOnly = true)
  public DashboardResponse dashboard(Long actorId, Long empresaId) {
    Rol rol = acceso.exigirMiembro(actorId, empresaId);
    long totalClientes = clientes.countByEmpresaIdAndEliminadoFalse(empresaId);
    Map<EstadoContrato, Long> porEstado =
        contarPorEstado(contratos.findByEmpresaIdAndEliminadoFalse(empresaId));
    return new DashboardResponse(
        empresaId,
        rol,
        totalClientes,
        porEstado.get(EstadoContrato.ACTIVO),
        porEstado.get(EstadoContrato.POR_VENCER),
        porEstado.get(EstadoContrato.EXPIRADO));
  }

  Map<EstadoContrato, Long> contarPorEstado(List<Contrato> lista) {
    Map<EstadoContrato, Long> conteo = new EnumMap<>(EstadoContrato.class);
    for (EstadoContrato estado : EstadoContrato.values()) {
      conteo.put(estado, 0L);
    }
    LocalDate hoy = LocalDate.now(clock);
    for (Contrato contrato : lista) {
      EstadoContrato estado = EstadoContrato.calcular(contrato.getFechaTermino(), hoy, diasAviso);
      conteo.merge(estado, 1L, Long::sum);
    }
    return conteo;
  }
}
