package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.EstadoContrato;
import java.time.LocalDate;

/**
 * Datos de un contrato con su estado calculado (RF-5.4).
 *
 * @param id identificador
 * @param clienteId cliente
 * @param titulo titulo
 * @param descripcion descripcion
 * @param fechaInicio inicio
 * @param fechaTermino termino
 * @param estado estado calculado
 * @param tieneDocumento si tiene documento adjunto
 */
public record ContratoResponse(
    Long id,
    Long clienteId,
    String titulo,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaTermino,
    EstadoContrato estado,
    boolean tieneDocumento) {}
