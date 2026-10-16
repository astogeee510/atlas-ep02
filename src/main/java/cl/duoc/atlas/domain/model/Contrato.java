package cl.duoc.atlas.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Contrato asociado a un unico cliente (RF-5.1, RB-3). Su estado no se guarda: se calcula a partir
 * de las fechas cada vez que se consulta (RF-5.4).
 */
public class Contrato {

  private Long id;
  private Long clienteId;
  private String titulo;
  private String descripcion;
  private LocalDate fechaInicio;
  private LocalDate fechaTermino;

  public Contrato() {}

  public Contrato(
      Long id,
      Long clienteId,
      String titulo,
      String descripcion,
      LocalDate fechaInicio,
      LocalDate fechaTermino) {
    this.id = id;
    this.clienteId = clienteId;
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.fechaInicio = fechaInicio;
    this.fechaTermino = fechaTermino;
  }

  /**
   * Indica si las fechas son coherentes.
   *
   * @return true si la fecha de termino no es anterior a la fecha de inicio
   */
  public boolean tieneFechasValidas() {
    return !fechaTermino.isBefore(fechaInicio);
  }

  /**
   * Calcula el estado del contrato a la fecha indicada.
   *
   * @param hoy fecha de referencia
   * @return ACTIVO, POR_VENCER o EXPIRADO
   */
  public EstadoContrato estado(LocalDate hoy) {
    return EstadoContrato.calcular(fechaTermino, hoy);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
    this.clienteId = clienteId;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaTermino() {
    return fechaTermino;
  }

  public void setFechaTermino(LocalDate fechaTermino) {
    this.fechaTermino = fechaTermino;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Contrato)) {
      return false;
    }
    Contrato contrato = (Contrato) o;
    return Objects.equals(id, contrato.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
