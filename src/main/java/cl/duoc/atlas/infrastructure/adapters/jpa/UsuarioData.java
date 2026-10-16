package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.model.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad JPA de usuarios. Solo guarda el hash bcrypt de la contrasena (NFR-SEG-2). */
@Entity
@Table(name = "usuarios")
public class UsuarioData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
  private String nombreUsuario;

  @Column(name = "hash_contrasena", nullable = false, length = 72)
  private String hashContrasena;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Rol rol;

  @Column(nullable = false)
  private boolean activo;

  public UsuarioData() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombreUsuario() {
    return nombreUsuario;
  }

  public void setNombreUsuario(String nombreUsuario) {
    this.nombreUsuario = nombreUsuario;
  }

  public String getHashContrasena() {
    return hashContrasena;
  }

  public void setHashContrasena(String hashContrasena) {
    this.hashContrasena = hashContrasena;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }
}
