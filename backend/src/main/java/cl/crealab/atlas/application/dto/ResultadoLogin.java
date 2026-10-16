package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.infrastructure.security.TokenEmitido;

/**
 * Resultado interno del login: la sesion (cuerpo JSON) y el token de refresco (cookie HttpOnly).
 *
 * @param sesion datos de sesion
 * @param refresco token de refresco
 */
public record ResultadoLogin(SesionResponse sesion, TokenEmitido refresco) {}
