package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un cliente (RF-4.1). Las validaciones de formato rechazan entradas
 * con caracteres de marcado HTML (NFR-SEG-5) y aplican formato a RUT, correo y telefono (RF-4.3).
 *
 * @param rut RUT con digito verificador
 * @param nombre nombre o razon social
 * @param descripcion descripcion opcional
 * @param direccion direccion comercial
 * @param email correo de contacto
 * @param telefono telefono de contacto
 */
@Schema(description = "Datos para crear o actualizar un cliente")
public record ClienteRequest(
    @NotBlank(message = "El RUT es obligatorio")
        @Pattern(
            regexp = "^[0-9.]{7,10}-?[0-9kK]$",
            message = "El RUT debe tener el formato 12.345.678-5")
        @Schema(example = "12.345.678-5")
        String rut,
    @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(
            regexp = "^[\\p{L}0-9 .,&'()-]+$",
            message = "El nombre solo admite letras, números y los signos . , & ' ( ) -")
        @Schema(example = "Ferretería Los Andes")
        String nombre,
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "La descripción no puede contener < ni >")
        @Schema(example = "Cliente mayorista")
        String descripcion,
    @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 150, message = "La dirección no puede superar 150 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "La dirección no puede contener < ni >")
        @Schema(example = "Av. Matta 500, Santiago")
        String direccion,
    @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        @Schema(example = "contacto@losandes.cl")
        String email,
    @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
            regexp = "^\\+?[0-9]{8,12}$",
            message = "El teléfono debe tener entre 8 y 12 dígitos (opcionalmente con +)")
        @Schema(example = "+56912345678")
        String telefono) {

  public Cliente toDomain() {
    return new Cliente(null, rut, nombre, descripcion, direccion, email, telefono, true);
  }
}
