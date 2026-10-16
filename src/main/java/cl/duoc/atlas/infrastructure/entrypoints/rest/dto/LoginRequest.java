package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de inicio de sesion (RF-1.1).
 *
 * @param nombreUsuario nombre de usuario
 * @param contrasena contrasena (maximo 72 caracteres por el limite de bcrypt)
 */
@Schema(description = "Credenciales de inicio de sesión")
public record LoginRequest(
    @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 50, message = "El nombre de usuario no puede superar 50 caracteres")
        @Schema(example = "admin")
        String nombreUsuario,
    @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
        @Schema(example = "Admin#Atlas2026")
        String contrasena) {}
