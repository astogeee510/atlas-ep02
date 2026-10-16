package cl.duoc.atlas.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Estado de un contrato calculado segun su fecha de termino (RF-5.4). */
public enum EstadoContrato {
  ACTIVO,
  POR_VENCER,
  EXPIRADO;

  /** Dias antes del termino en que un contrato pasa a "por vencer". */
  public static final int DIAS_AVISO = 30;

  /**
   * Calcula el estado de un contrato.
   *
   * <ul>
   *   <li>EXPIRADO: la fecha de termino ya paso.
   *   <li>POR_VENCER: faltan {@value #DIAS_AVISO} dias o menos para el termino.
   *   <li>ACTIVO: en cualquier otro caso.
   * </ul>
   *
   * @param fechaTermino fecha de termino del contrato
   * @param hoy fecha de referencia
   * @return el estado calculado
   */
  public static EstadoContrato calcular(LocalDate fechaTermino, LocalDate hoy) {
    if (hoy.isAfter(fechaTermino)) {
      return EXPIRADO;
    }
    long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaTermino);
    if (diasRestantes <= DIAS_AVISO) {
      return POR_VENCER;
    }
    return ACTIVO;
  }
}
