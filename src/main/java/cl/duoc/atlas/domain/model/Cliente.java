package cl.duoc.atlas.domain.model;

import java.util.Objects;

/**
 * Cliente de la empresa (RF-4). Modelo de dominio sin dependencias de frameworks.
 *
 * <p>La eliminacion es logica: un cliente eliminado queda con {@code activo = false} y deja de
 * mostrarse, pero se conserva para la trazabilidad (criterio de aceptacion de RF-4).
 */
public class Cliente {

  private Long id;
  private String rut;
  private String nombre;
  private String descripcion;
  private String direccion;
  private String email;
  private String telefono;
  private boolean activo = true;

  public Cliente() {}

  public Cliente(
      Long id,
      String rut,
      String nombre,
      String descripcion,
      String direccion,
      String email,
      String telefono,
      boolean activo) {
    this.id = id;
    this.rut = rut;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.direccion = direccion;
    this.email = email;
    this.telefono = telefono;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRut() {
    return rut;
  }

  public void setRut(String rut) {
    this.rut = rut;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Cliente)) {
      return false;
    }
    Cliente cliente = (Cliente) o;
    return Objects.equals(id, cliente.id) && Objects.equals(rut, cliente.rut);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, rut);
  }
}
