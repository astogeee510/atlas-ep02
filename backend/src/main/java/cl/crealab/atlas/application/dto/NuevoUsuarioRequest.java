package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta o invitacion de un usuario a la empresa (RF-3.1). Si el usuario ya existe, basta el nombre;
 * si es nuevo, se requieren correo y contrasena.
 *
 * @param nombreUsuario nombre de usuario
 * @param correo correo (solo usuario nuevo)
 * @param contrasena contrasena inicial (solo usuario nuevo)
 * @param rol rol asignado
 */
public record NuevoUsuarioRequest(
    @NotBlank @Pattern(regexp = Patrones.USUARIO) String nombreUsuario,
    @Email @Size(max = 120) String correo,
    @Size(max = 128) String contrasena,
    @NotNull Rol rol) {}
