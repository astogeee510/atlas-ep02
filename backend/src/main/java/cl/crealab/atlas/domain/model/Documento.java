package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Documento adjunto a un contrato, almacenado en storage interno (RF-5.2, RF-5.3). */
@Entity
@Table(name = "documento")
public class Documento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "contrato_id", nullable = false, unique = true)
  private Long contratoId;

  @Column(name = "nombre_original", nullable = false)
  private String nombreOriginal;

  @Column(name = "tipo_contenido", nullable = false, length = 120)
  private String tipoContenido;

  @Column(name = "tamano_bytes", nullable = false)
  private long tamanoBytes;

  @Column(name = "ruta_almacenamiento", nullable = false)
  private String rutaAlmacenamiento;

  @Column(nullable = false, length = 64)
  private String sha256;

  @Column(name = "subido_por", nullable = false)
  private Long subidoPor;

  @Column(name = "subido_en", nullable = false)
  private Instant subidoEn = Instant.now();

  public Documento() {
    // requerido por JPA
  }

  public Documento(Long contratoId, String nombreOriginal, String tipoContenido, Long subidoPor) {
    this.contratoId = contratoId;
    this.nombreOriginal = nombreOriginal;
    this.tipoContenido = tipoContenido;
    this.subidoPor = subidoPor;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getContratoId() {
    return contratoId;
  }

  public String getNombreOriginal() {
    return nombreOriginal;
  }

  public String getTipoContenido() {
    return tipoContenido;
  }

  public long getTamanoBytes() {
    return tamanoBytes;
  }

  public void setTamanoBytes(long tamanoBytes) {
    this.tamanoBytes = tamanoBytes;
  }

  public String getRutaAlmacenamiento() {
    return rutaAlmacenamiento;
  }

  public void setRutaAlmacenamiento(String rutaAlmacenamiento) {
    this.rutaAlmacenamiento = rutaAlmacenamiento;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

  public Long getSubidoPor() {
    return subidoPor;
  }

  public Instant getSubidoEn() {
    return subidoEn;
  }
}
