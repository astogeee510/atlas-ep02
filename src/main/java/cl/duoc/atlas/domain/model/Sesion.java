package cl.duoc.atlas.domain.model;

import java.time.Instant;

/**
 * Sesion emitida al iniciar sesion: un JWT de acceso de corta duracion (NFR-SEG-9).
 *
 * @param token JWT firmado
 * @param expiraEn instante de expiracion del token
 * @param nombreUsuario usuario autenticado
 * @param rol rol del usuario
 */
public record Sesion(String token, Instant expiraEn, String nombreUsuario, Rol rol) {}
