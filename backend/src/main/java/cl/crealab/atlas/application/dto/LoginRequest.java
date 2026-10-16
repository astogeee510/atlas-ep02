package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de inicio de sesion (RF-1.1).
 *
 * @param nombreUsuario nombre de usuario
 * @param contrasena contrasena
 */
public record LoginRequest(
    @NotBlank @Size(max = 50) String nombreUsuario, @NotBlank @Size(max = 128) String contrasena) {}
