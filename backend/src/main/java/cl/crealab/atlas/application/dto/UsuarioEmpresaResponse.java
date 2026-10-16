package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Rol;

/**
 * Usuario de la empresa con su rol.
 *
 * @param usuarioId id del usuario
 * @param nombreUsuario nombre de usuario
 * @param correo correo
 * @param rol rol en la empresa
 */
public record UsuarioEmpresaResponse(
    Long usuarioId, String nombreUsuario, String correo, Rol rol) {}
