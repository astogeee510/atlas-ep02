package cl.crealab.atlas.domain.model;

import cl.crealab.atlas.infrastructure.crypto.CampoCifradoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Cliente de una empresa (RF-4). Los datos de contacto y bancarios se cifran en reposo con
 * AES-256-GCM (NFR-SEG-3). La eliminacion es logica (criterio de aceptacion RF-4).
 */
@Entity
@Table(name = "cliente")
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "empresa_id", nullable = false)
  private Long empresaId;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(length = 12)
  private String rut;

  @Column(length = 500)
  private String descripcion;

  @Column(length = 200)
  private String direccion;

  @Convert(converter = CampoCifradoConverter.class)
  @Column(name = "correo_contacto", length = 512)
  private String correoContacto;

  @Convert(converter = CampoCifradoConverter.class)
  @Column(name = "telefono_contacto", length = 512)
  private String telefonoContacto;

  @Convert(converter = CampoCifradoConverter.class)
  @Column(length = 512)
  private String banco;

  @Convert(converter = CampoCifradoConverter.class)
  @Column(name = "numero_cuenta", length = 512)
  private String numeroCuenta;

  @Column(nullable = false)
  private boolean eliminado;

  @Column(name = "eliminado_en")
  private Instant eliminadoEn;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  @Column(name = "actualizado_en", nullable = false)
  private Instant actualizadoEn = Instant.now();

  public Cliente() {
    // requerido por JPA
  }

  public Cliente(Long empresaId, String nombre) {
    this.empresaId = empresaId;
    this.nombre = nombre;
  }

  /** Marca el cliente como eliminado sin borrarlo fisicamente. */
  public void eliminarLogicamente(Instant ahora) {
    this.eliminado = true;
    this.eliminadoEn = ahora;
    this.actualizadoEn = ahora;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEmpresaId() {
    return empresaId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getRut() {
    return rut;
  }

  public void setRut(String rut) {
    this.rut = rut;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getCorreoContacto() {
    return correoContacto;
  }

  public void setCorreoContacto(String correoContacto) {
    this.correoContacto = correoContacto;
  }

  public String getTelefonoContacto() {
    return telefonoContacto;
  }

  public void setTelefonoContacto(String telefonoContacto) {
    this.telefonoContacto = telefonoContacto;
  }

  public String getBanco() {
    return banco;
  }

  public void setBanco(String banco) {
    this.banco = banco;
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public void setNumeroCuenta(String numeroCuenta) {
    this.numeroCuenta = numeroCuenta;
  }

  public boolean isEliminado() {
    return eliminado;
  }

  public Instant getEliminadoEn() {
    return eliminadoEn;
  }

  public Instant getActualizadoEn() {
    return actualizadoEn;
  }

  public void setActualizadoEn(Instant actualizadoEn) {
    this.actualizadoEn = actualizadoEn;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
