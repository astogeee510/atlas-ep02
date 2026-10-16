package cl.crealab.atlas.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Estado de un contrato calculado a partir de sus fechas (RF-5.4). */
public enum EstadoContrato {
  ACTIVO,
  POR_VENCER,
  EXPIRADO;

  /**
   * Calcula el estado de un contrato.
   *
   * @param fechaTermino fecha de termino del contrato
   * @param hoy fecha de referencia
   * @param diasAviso dias antes del termino en que el contrato pasa a "por vencer"
   * @return el estado calculado
   */
  public static EstadoContrato calcular(LocalDate fechaTermino, LocalDate hoy, int diasAviso) {
    if (hoy.isAfter(fechaTermino)) {
      return EXPIRADO;
    }
    long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaTermino);
    if (diasRestantes <= diasAviso) {
      return POR_VENCER;
    }
    return ACTIVO;
  }
}
