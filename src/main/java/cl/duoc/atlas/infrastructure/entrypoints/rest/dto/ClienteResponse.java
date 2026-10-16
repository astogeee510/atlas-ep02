package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cliente devuelto por la API.
 *
 * @param id identificador
 * @param rut RUT normalizado
 * @param nombre nombre o razon social
 * @param descripcion descripcion
 * @param direccion direccion
 * @param email correo de contacto
 * @param telefono telefono de contacto
 */
@Schema(description = "Cliente devuelto por la API")
public record ClienteResponse(
    Long id,
    String rut,
    String nombre,
    String descripcion,
    String direccion,
    String email,
    String telefono) {

  public static ClienteResponse fromDomain(Cliente cliente) {
    return new ClienteResponse(
        cliente.getId(),
        cliente.getRut(),
        cliente.getNombre(),
        cliente.getDescripcion(),
        cliente.getDireccion(),
        cliente.getEmail(),
        cliente.getTelefono());
  }
}
