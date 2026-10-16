package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.AuditoriaResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.RegistroAuditoria;
import cl.crealab.atlas.infrastructure.persistence.AuditoriaRepository;
import java.time.Clock;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Registro y consulta de la auditoria de acciones criticas (RF-6.1). */
@Service
public class AuditoriaService {

  private final AuditoriaRepository repositorio;
  private final ControlAcceso acceso;
  private final Clock clock;

  public AuditoriaService(AuditoriaRepository repositorio, ControlAcceso acceso, Clock clock) {
    this.repositorio = repositorio;
    this.acceso = acceso;
    this.clock = clock;
  }

  /**
   * Registra una accion critica.
   *
   * @param empresaId empresa afectada (puede ser null)
   * @param usuarioId usuario que actua (null si es por enlace publico)
   * @param accion accion
   * @param entidad tipo de entidad
   * @param entidadId id de la entidad
   * @param detalle detalle breve, sin datos sensibles
   */
  public void registrar(
      Long empresaId,
      Long usuarioId,
      AccionAuditoria accion,
      String entidad,
      Long entidadId,
      String detalle) {
    repositorio.save(
        new RegistroAuditoria(
            empresaId, usuarioId, accion, entidad, entidadId, detalle, clock.instant()));
  }

  /**
   * Lista la auditoria de una empresa (solo administradores).
   *
   * @param actorId usuario que consulta
   * @param empresaId empresa
   * @param pagina pagina
   * @param tamano tamano de pagina
   * @return pagina de registros, del mas reciente al mas antiguo
   */
  public PaginaResponse<AuditoriaResponse> listar(
      Long actorId, Long empresaId, int pagina, int tamano) {
    acceso.exigirAdmin(actorId, empresaId);
    return PaginaResponse.de(
        repositorio.findByEmpresaIdOrderByFechaDesc(
            empresaId,
            PageRequest.of(
                PaginaResponse.paginaSegura(pagina), PaginaResponse.tamanoSeguro(tamano))),
        AuditoriaResponse::desde);
  }
}
