package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Empresa (pyme) que gestiona sus clientes en Atlas (RF-2.2). */
@Entity
@Table(name = "empresa")
public class Empresa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(length = 200)
  private String direccion;

  @Column(length = 20)
  private String telefono;

  @Column(nullable = false, length = 120)
  private String correo;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  public Empresa() {
    // requerido por JPA
  }

  public Empresa(String nombre, String direccion, String telefono, String correo) {
    this.nombre = nombre;
    this.direccion = direccion;
    this.telefono = telefono;
    this.correo = correo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDireccion() {
    return direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
