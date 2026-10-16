package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Contrato;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Datos para crear o actualizar un contrato (RF-5.1).
 *
 * @param titulo titulo del contrato
 * @param descripcion descripcion opcional
 * @param fechaInicio fecha de inicio (yyyy-MM-dd)
 * @param fechaTermino fecha de termino (yyyy-MM-dd), no anterior a la de inicio
 */
@Schema(description = "Datos para crear o actualizar un contrato")
public record ContratoRequest(
    @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar 120 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "El título no puede contener < ni >")
        @Schema(example = "Suministro anual 2026")
        String titulo,
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "La descripción no puede contener < ni >")
        @Schema(example = "Entrega mensual de insumos")
        String descripcion,
    @NotNull(message = "La fecha de inicio es obligatoria") @Schema(example = "2026-01-01")
        LocalDate fechaInicio,
    @NotNull(message = "La fecha de término es obligatoria") @Schema(example = "2026-12-31")
        LocalDate fechaTermino) {

  public Contrato toDomain() {
    return new Contrato(null, null, titulo, descripcion, fechaInicio, fechaTermino);
  }
}
