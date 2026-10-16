package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Cliente;
import cl.crealab.atlas.domain.model.Rol;
import java.time.Instant;

/**
 * Datos de un cliente. Para el rol EDITOR el numero de cuenta se enmascara (minimizacion, Ley
 * 19.628).
 *
 * @param id identificador
 * @param empresaId empresa
 * @param nombre nombre
 * @param rut RUT
 * @param descripcion descripcion
 * @param direccion direccion
 * @param correoContacto correo de contacto
 * @param telefonoContacto telefono de contacto
 * @param banco banco
 * @param numeroCuenta numero de cuenta (enmascarado para EDITOR)
 * @param actualizadoEn fecha de ultima modificacion
 */
public record ClienteResponse(
    Long id,
    Long empresaId,
    String nombre,
    String rut,
    String descripcion,
    String direccion,
    String correoContacto,
    String telefonoContacto,
    String banco,
    String numeroCuenta,
    Instant actualizadoEn) {

  /** Crea la respuesta segun el rol de quien consulta. */
  public static ClienteResponse desde(Cliente c, Rol rol) {
    String cuenta = rol == Rol.ADMIN ? c.getNumeroCuenta() : enmascarar(c.getNumeroCuenta());
    return new ClienteResponse(
        c.getId(),
        c.getEmpresaId(),
        c.getNombre(),
        c.getRut(),
        c.getDescripcion(),
        c.getDireccion(),
        c.getCorreoContacto(),
        c.getTelefonoContacto(),
        c.getBanco(),
        cuenta,
        c.getActualizadoEn());
  }

  /** Deja visibles solo los ultimos 4 caracteres. */
  public static String enmascarar(String valor) {
    if (valor == null || valor.length() <= 4) {
      return valor == null ? null : "****";
    }
    return "****" + valor.substring(valor.length() - 4);
  }
}
