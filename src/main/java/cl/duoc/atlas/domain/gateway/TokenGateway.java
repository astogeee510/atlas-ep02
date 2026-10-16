package cl.duoc.atlas.domain.gateway;

import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;

/** Puerto para emitir y revocar tokens de sesion (RF-1.1, RF-1.2). */
public interface TokenGateway {

  /**
   * Emite un token de acceso para el usuario.
   *
   * @param usuario usuario autenticado
   * @return sesion con el token y su expiracion
   */
  Sesion emitir(Usuario usuario);

  /**
   * Revoca un token para que no pueda volver a usarse aunque no haya expirado (logout).
   *
   * @param token JWT a revocar
   */
  void revocar(String token);
}
