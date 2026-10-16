package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Rol;

/**
 * Empresa a la que pertenece el usuario y su rol en ella.
 *
 * @param empresaId id de la empresa
 * @param empresaNombre nombre de la empresa
 * @param rol rol del usuario
 */
public record MembresiaResponse(Long empresaId, String empresaNombre, Rol rol) {}
