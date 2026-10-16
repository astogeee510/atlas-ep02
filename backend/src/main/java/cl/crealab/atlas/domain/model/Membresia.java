package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Relacion usuario-empresa con su rol (tabla empresa_usuario, RB-2). */
@Entity
@Table(name = "empresa_usuario")
public class Membresia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "empresa_id", nullable = false)
  private Long empresaId;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Rol rol;

  public Membresia() {
    // requerido por JPA
  }

  public Membresia(Long empresaId, Long usuarioId, Rol rol) {
    this.empresaId = empresaId;
    this.usuarioId = usuarioId;
    this.rol = rol;
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

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }
}
