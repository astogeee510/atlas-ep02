package cl.crealab.atlas.infrastructure.rest;

import java.time.Instant;
import java.util.List;

/**
 * Cuerpo de error uniforme. Nunca incluye trazas ni detalles internos (NFR-SEG-6).
 *
 * @param status codigo HTTP
 * @param error nombre del error
 * @param mensaje mensaje amigable para el usuario (NFR-USAB-3)
 * @param ruta ruta solicitada
 * @param detalles detalle de validaciones
 * @param fecha instante del error
 */
public record ErrorResponse(
    int status, String error, String mensaje, String ruta, List<String> detalles, Instant fecha) {}
