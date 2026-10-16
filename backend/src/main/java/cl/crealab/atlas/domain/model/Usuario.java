package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Usuario registrado en Atlas (RF-2.1). */
@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
  private String nombreUsuario;

  @Column(nullable = false, unique = true, length = 120)
  private String correo;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(name = "consentimiento_datos", nullable = false)
  private boolean consentimientoDatos;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  public Usuario() {
    // requerido por JPA
  }

  public Usuario(String nombreUsuario, String correo, String passwordHash, boolean consentimiento) {
    this.nombreUsuario = nombreUsuario;
    this.correo = correo;
    this.passwordHash = passwordHash;
    this.consentimientoDatos = consentimiento;
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

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  public boolean isConsentimientoDatos() {
    return consentimientoDatos;
  }

  public void setConsentimientoDatos(boolean consentimientoDatos) {
    this.consentimientoDatos = consentimientoDatos;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
