package cl.duoc.atlas.infrastructure.security;

import java.time.Instant;

/**
 * Datos de un JWT de acceso cuya firma, expiracion y estado (no revocado) fueron verificados.
 *
 * @param nombreUsuario usuario del token (claim sub)
 * @param rol rol del usuario (claim rol)
 * @param jti identificador unico del token
 * @param expiraEn instante de expiracion
 */
public record TokenValido(String nombreUsuario, String rol, String jti, Instant expiraEn) {}
