package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Contrato asociado a un unico cliente (RF-5.1, RB-3). */
@Entity
@Table(name = "contrato")
public class Contrato {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "empresa_id", nullable = false)
  private Long empresaId;

  @Column(name = "cliente_id", nullable = false)
  private Long clienteId;

  @Column(nullable = false, length = 150)
  private String titulo;

  @Column(length = 1000)
  private String descripcion;

  @Column(name = "fecha_inicio", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "fecha_termino", nullable = false)
  private LocalDate fechaTermino;

  @Column(nullable = false)
  private boolean eliminado;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  public Contrato() {
    // requerido por JPA
  }

  public Contrato(Long empresaId, Long clienteId, String titulo) {
    this.empresaId = empresaId;
    this.clienteId = clienteId;
    this.titulo = titulo;
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

  public Long getClienteId() {
    return clienteId;
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

  public boolean isEliminado() {
    return eliminado;
  }

  public void setEliminado(boolean eliminado) {
    this.eliminado = eliminado;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
