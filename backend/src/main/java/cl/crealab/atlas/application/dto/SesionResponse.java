package cl.crealab.atlas.application.dto;

import java.util.List;

/**
 * Respuesta de login o refresco (RF-1.1).
 *
 * @param accessToken JWT de acceso
 * @param tipo siempre "Bearer"
 * @param expiraEnSegundos vigencia del token de acceso
 * @param usuario datos del usuario
 * @param empresas empresas y roles del usuario
 */
public record SesionResponse(
    String accessToken,
    String tipo,
    long expiraEnSegundos,
    UsuarioResponse usuario,
    List<MembresiaResponse> empresas) {}
