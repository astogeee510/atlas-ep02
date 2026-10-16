package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.RegistroAuditoria;
import java.time.Instant;

/**
 * Registro de auditoria (RF-6.1).
 *
 * @param id identificador
 * @param usuarioId usuario que realizo la accion (null si fue por enlace publico)
 * @param accion accion
 * @param entidad entidad afectada
 * @param entidadId id de la entidad
 * @param detalle detalle
 * @param fecha fecha
 */
public record AuditoriaResponse(
    Long id,
    Long usuarioId,
    AccionAuditoria accion,
    String entidad,
    Long entidadId,
    String detalle,
    Instant fecha) {

  /** Crea la respuesta a partir de la entidad. */
  public static AuditoriaResponse desde(RegistroAuditoria r) {
    return new AuditoriaResponse(
        r.getId(),
        r.getUsuarioId(),
        r.getAccion(),
        r.getEntidad(),
        r.getEntidadId(),
        r.getDetalle(),
        r.getFecha());
  }
}
