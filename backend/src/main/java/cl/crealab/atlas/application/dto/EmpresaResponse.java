package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Empresa;

/**
 * Datos de una empresa.
 *
 * @param id identificador
 * @param nombre nombre
 * @param direccion direccion
 * @param telefono telefono
 * @param correo correo principal
 */
public record EmpresaResponse(
    Long id, String nombre, String direccion, String telefono, String correo) {

  /** Crea la respuesta a partir de la entidad. */
  public static EmpresaResponse desde(Empresa e) {
    return new EmpresaResponse(
        e.getId(), e.getNombre(), e.getDireccion(), e.getTelefono(), e.getCorreo());
  }
}
