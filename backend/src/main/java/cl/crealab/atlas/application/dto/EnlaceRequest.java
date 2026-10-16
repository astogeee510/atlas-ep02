package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Parametros del enlace para compartir (RF-5.5).
 *
 * @param horas vigencia en horas (1 a 168; por defecto 24)
 */
public record EnlaceRequest(@Min(1) @Max(168) Integer horas) {}
