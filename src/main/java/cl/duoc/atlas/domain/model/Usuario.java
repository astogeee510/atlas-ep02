package cl.duoc.atlas.domain.model;

/**
 * Usuario que puede iniciar sesion (RF-1). La contrasena nunca se guarda en claro: solo su hash
 * bcrypt (NFR-SEG-2).
 */
public class Usuario {

  private Long id;
  private String nombreUsuario;
  private String hashContrasena;
  private Rol rol;
  private boolean activo = true;

  public Usuario() {}

  public Usuario(Long id, String nombreUsuario, String hashContrasena, Rol rol, boolean activo) {
    this.id = id;
    this.nombreUsuario = nombreUsuario;
    this.hashContrasena = hashContrasena;
    this.rol = rol;
    this.activo = activo;
  }

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
