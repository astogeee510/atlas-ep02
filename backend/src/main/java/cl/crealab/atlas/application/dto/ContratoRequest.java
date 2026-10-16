package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Datos de un contrato (RF-5.1).
 *
 * @param clienteId cliente unico del contrato (RB-3)
 * @param titulo titulo
 * @param descripcion descripcion
 * @param fechaInicio fecha de inicio
 * @param fechaTermino fecha de termino
 */
public record ContratoRequest(
    @NotNull Long clienteId,
    @NotBlank @Size(max = 150) String titulo,
    @Size(max = 1000) String descripcion,
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaTermino) {}
