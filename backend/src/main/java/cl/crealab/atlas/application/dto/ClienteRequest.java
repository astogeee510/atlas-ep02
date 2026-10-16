package cl.crealab.atlas.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de un cliente (RF-4.1, RF-4.3).
 *
 * @param nombre nombre o razon social
 * @param rut RUT (opcional, se valida digito verificador)
 * @param descripcion descripcion
 * @param direccion direccion
 * @param correoContacto correo de contacto
 * @param telefonoContacto telefono de contacto
 * @param banco banco (dato bancario, cifrado en reposo)
 * @param numeroCuenta numero de cuenta (dato bancario, cifrado en reposo)
 */
public record ClienteRequest(
    @NotBlank @Pattern(regexp = Patrones.NOMBRE, message = "Nombre con caracteres no permitidos")
        String nombre,
    @Size(max = 12) String rut,
    @Size(max = 500) String descripcion,
    @Size(max = 200) String direccion,
    @Email @Size(max = 120) String correoContacto,
    @Pattern(regexp = Patrones.TELEFONO, message = "Telefono invalido") String telefonoContacto,
    @Pattern(regexp = Patrones.BANCO, message = "Banco invalido") String banco,
    @Pattern(regexp = Patrones.CUENTA, message = "Cuenta: solo numeros y guiones")
        String numeroCuenta) {}
