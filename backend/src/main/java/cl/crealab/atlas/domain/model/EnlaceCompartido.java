package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Enlace temporal y revocable para compartir el documento de un contrato (RF-5.5). */
@Entity
@Table(name = "enlace_compartido")
public class EnlaceCompartido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "documento_id", nullable = false)
  private Long documentoId;

  @Column(nullable = false, unique = true, length = 64)
  private String token;

  @Column(name = "expira_en", nullable = false)
  private Instant expiraEn;

  @Column(nullable = false)
  private boolean revocado;

  @Column(name = "creado_por", nullable = false)
  private Long creadoPor;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  public EnlaceCompartido() {
    // requerido por JPA
  }

  public EnlaceCompartido(Long documentoId, String token, Instant expiraEn, Long creadoPor) {
    this.documentoId = documentoId;
    this.token = token;
    this.expiraEn = expiraEn;
    this.creadoPor = creadoPor;
  }

  /** Indica si el enlace puede usarse en el instante indicado. */
  public boolean esVigente(Instant ahora) {
    return !revocado && ahora.isBefore(expiraEn);
  }

  public Long getId() {
    return id;
  }

  public Long getDocumentoId() {
    return documentoId;
  }

  public String getToken() {
    return token;
  }

  public Instant getExpiraEn() {
    return expiraEn;
  }

  public boolean isRevocado() {
    return revocado;
  }

  public void setRevocado(boolean revocado) {
    this.revocado = revocado;
  }

  public Long getCreadoPor() {
    return creadoPor;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
