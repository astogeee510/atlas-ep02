package cl.crealab.atlas.application.dto;

import java.util.List;

/**
 * Perfil del usuario autenticado.
 *
 * @param usuario datos del usuario
 * @param empresas empresas y roles
 */
public record PerfilResponse(UsuarioResponse usuario, List<MembresiaResponse> empresas) {}
