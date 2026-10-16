package cl.crealab.atlas.infrastructure.security;

import java.time.Instant;

/**
 * Datos del usuario extraidos de un JWT valido.
 *
 * @param id identificador del usuario
 * @param nombreUsuario nombre de usuario
 * @param jti identificador unico del token (para revocarlo)
 * @param expiraEn instante de expiracion del token
 */
public record UsuarioAutenticado(Long id, String nombreUsuario, String jti, Instant expiraEn) {}
