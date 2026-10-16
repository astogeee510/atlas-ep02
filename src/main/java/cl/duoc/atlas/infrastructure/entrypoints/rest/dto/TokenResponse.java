package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Sesion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Respuesta del login con el JWT de acceso (NFR-SEG-9).
 *
 * @param accessToken JWT a enviar en el header Authorization
 * @param tipo siempre "Bearer"
 * @param expiraEn instante de expiracion
 * @param usuario usuario autenticado
 * @param rol rol del usuario
 */
@Schema(description = "Token de acceso emitido al iniciar sesión")
public record TokenResponse(
    String accessToken, String tipo, Instant expiraEn, String usuario, String rol) {

  public static TokenResponse fromDomain(Sesion sesion) {
    return new TokenResponse(
        sesion.token(), "Bearer", sesion.expiraEn(), sesion.nombreUsuario(), sesion.rol().name());
  }
}
