package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.EstadoContrato;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Contrato devuelto por la API con su estado calculado (RF-5.4).
 *
 * @param id identificador
 * @param clienteId cliente al que pertenece
 * @param titulo titulo
 * @param descripcion descripcion
 * @param fechaInicio fecha de inicio
 * @param fechaTermino fecha de termino
 * @param estado ACTIVO, POR_VENCER o EXPIRADO
 */
@Schema(description = "Contrato devuelto por la API")
public record ContratoResponse(
    Long id,
    Long clienteId,
    String titulo,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaTermino,
    EstadoContrato estado) {

  public static ContratoResponse fromDomain(Contrato contrato, EstadoContrato estado) {
    return new ContratoResponse(
        contrato.getId(),
        contrato.getClienteId(),
        contrato.getTitulo(),
        contrato.getDescripcion(),
        contrato.getFechaInicio(),
        contrato.getFechaTermino(),
        estado);
  }
}
