package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una empresa (RF-2.2).
 *
 * @param nombre nombre
 * @param direccion direccion
 * @param telefono telefono
 * @param correo correo principal (unico, RB-1)
 */
public record EmpresaRequest(
    @NotBlank @Pattern(regexp = Patrones.NOMBRE, message = "Nombre con caracteres no permitidos")
        String nombre,
    @Size(max = 200) String direccion,
    @Pattern(regexp = Patrones.TELEFONO, message = "Telefono invalido") String telefono,
    @NotBlank @Email @Size(max = 120) String correo) {}
