package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Usuario;

/**
 * Datos publicos de un usuario (nunca incluye el hash de contrasena).
 *
 * @param id identificador
 * @param nombreUsuario nombre de usuario
 * @param correo correo
 */
public record UsuarioResponse(Long id, String nombreUsuario, String correo) {

  /** Crea la respuesta a partir de la entidad. */
  public static UsuarioResponse desde(Usuario usuario) {
    return new UsuarioResponse(usuario.getId(), usuario.getNombreUsuario(), usuario.getCorreo());
  }
}
