package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registro rapido de usuario (RF-2.1). El consentimiento es obligatorio (Ley 19.628 / 21.719).
 *
 * @param nombreUsuario nombre de usuario
 * @param correo correo electronico
 * @param contrasena contrasena en texto plano (solo viaja por TLS)
 * @param consentimiento aceptacion del tratamiento de datos personales
 */
public record RegistroRequest(
    @NotBlank
        @Pattern(regexp = Patrones.USUARIO, message = "Usuario: 4 a 50 letras, numeros, . _ -")
        String nombreUsuario,
    @NotBlank @Email @Size(max = 120) String correo,
    @NotBlank @Size(max = 128) String contrasena,
    @AssertTrue(message = "Debe aceptar la politica de privacidad") boolean consentimiento) {}
