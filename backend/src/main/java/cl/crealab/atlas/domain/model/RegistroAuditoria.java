package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Registro de auditoria de una accion critica (RF-6.1). */
@Entity
@Table(name = "auditoria")
public class RegistroAuditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "empresa_id")
  private Long empresaId;

  @Column(name = "usuario_id")
  private Long usuarioId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private AccionAuditoria accion;

  @Column(nullable = false, length = 40)
  private String entidad;

  @Column(name = "entidad_id")
  private Long entidadId;

  @Column(length = 255)
  private String detalle;

  @Column(nullable = false)
  private Instant fecha;

  public RegistroAuditoria() {
    // requerido por JPA
  }

  public RegistroAuditoria(
      Long empresaId,
      Long usuarioId,
      AccionAuditoria accion,
      String entidad,
      Long entidadId,
      String detalle,
      Instant fecha) {
    this.empresaId = empresaId;
    this.usuarioId = usuarioId;
    this.accion = accion;
    this.entidad = entidad;
    this.entidadId = entidadId;
    this.detalle = detalle;
    this.fecha = fecha;
  }

  public Long getId() {
    return id;
  }

  public Long getEmpresaId() {
    return empresaId;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public AccionAuditoria getAccion() {
    return accion;
  }

  public String getEntidad() {
    return entidad;
  }

  public Long getEntidadId() {
    return entidadId;
  }

  public String getDetalle() {
    return detalle;
  }

  public Instant getFecha() {
    return fecha;
  }
}
