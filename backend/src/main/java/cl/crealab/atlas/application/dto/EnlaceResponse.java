package cl.crealab.atlas.application.dto;

import java.time.Instant;

/**
 * Enlace generado para compartir un documento.
 *
 * @param token token aleatorio de 256 bits
 * @param url ruta relativa de descarga
 * @param expiraEn instante de expiracion
 */
public record EnlaceResponse(String token, String url, Instant expiraEn) {}
