package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Documento;
import java.time.Instant;

/**
 * Metadatos de un documento (RF-5.2).
 *
 * @param id identificador
 * @param contratoId contrato
 * @param nombreOriginal nombre original (saneado)
 * @param tipoContenido tipo MIME
 * @param tamanoBytes tamano
 * @param sha256 huella para verificar integridad
 * @param subidoEn fecha de subida
 */
public record DocumentoResponse(
    Long id,
    Long contratoId,
    String nombreOriginal,
    String tipoContenido,
    long tamanoBytes,
    String sha256,
    Instant subidoEn) {

  /** Crea la respuesta a partir de la entidad. */
  public static DocumentoResponse desde(Documento d) {
    return new DocumentoResponse(
        d.getId(),
        d.getContratoId(),
        d.getNombreOriginal(),
        d.getTipoContenido(),
        d.getTamanoBytes(),
        d.getSha256(),
        d.getSubidoEn());
  }
}
