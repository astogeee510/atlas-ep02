package cl.crealab.atlas.infrastructure.security;

import java.time.Instant;

/**
 * Token JWT recien firmado.
 *
 * @param token JWT compacto
 * @param jti identificador unico
 * @param expiraEn instante de expiracion
 */
public record TokenEmitido(String token, String jti, Instant expiraEn) {}
